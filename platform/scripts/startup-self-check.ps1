param(
    [string]$MysqlContainer = "codequest-mysql",
    [string]$BaseUrl = "http://localhost:8080/platform"
)

$ErrorActionPreference = "Stop"

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )
    if (-not $Condition) {
        throw $Message
    }
}

Write-Host "[1/5] Check MySQL container status..."
$running = docker inspect -f "{{.State.Running}}" $MysqlContainer 2>$null
Assert-True ($running -eq "true") "MySQL container is not running: $MysqlContainer"

Write-Host "[2/5] Check core table count..."
$tableCount = docker exec $MysqlContainer mysql -uroot -proot -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='codequest_db';"
Assert-True ([int]$tableCount -ge 6) "Missing core tables in codequest_db, current count: $tableCount"

Write-Host "[3/5] Check users and question data..."
$tqCount = docker exec $MysqlContainer mysql -uroot -proot -N -e "SELECT COUNT(*) FROM codequest_db.t_question;"
$sqCount = docker exec $MysqlContainer mysql -uroot -proot -N -e "SELECT COUNT(*) FROM codequest_db.sys_question;"
$oliveiraRole = docker exec $MysqlContainer mysql -uroot -proot -N -e "SELECT role FROM codequest_db.sys_user WHERE username='Oliveira' LIMIT 1;"
$testUserExists = docker exec $MysqlContainer mysql -uroot -proot -N -e "SELECT COUNT(*) FROM codequest_db.sys_user WHERE username='testuser';"
$promptExists = docker exec $MysqlContainer mysql -uroot -proot -N -e "SELECT COUNT(*) FROM codequest_db.sys_prompt_template WHERE template_name='evaluation_prompt';"

Assert-True ([int]$tqCount -ge 10) "Insufficient rows in t_question: $tqCount"
Assert-True ([int]$sqCount -ge 10) "Insufficient rows in sys_question: $sqCount"
Assert-True ($oliveiraRole -eq "admin") "Oliveira is not admin, current role: $oliveiraRole"
Assert-True ([int]$testUserExists -ge 1) "testuser does not exist"
Assert-True ([int]$promptExists -ge 1) "evaluation_prompt does not exist"

Write-Host "[4/5] Check login flow..."
$loginOliveira = curl.exe -i -s -o NUL -w "HTTP:%{http_code} REDIR:%{redirect_url}" -X POST "$BaseUrl/login" -H "Content-Type: application/x-www-form-urlencoded" --data "username=Oliveira&password=123456"
$loginTestuser = curl.exe -i -s -o NUL -w "HTTP:%{http_code} REDIR:%{redirect_url}" -X POST "$BaseUrl/login" -H "Content-Type: application/x-www-form-urlencoded" --data "username=testuser&password=123456"

Assert-True ($loginOliveira -like "HTTP:302*") "Oliveira login failed: $loginOliveira"
Assert-True ($loginTestuser -like "HTTP:302*") "testuser login failed: $loginTestuser"

Write-Host "[5/5] Check questions page availability..."
$questionsStatus = curl.exe -i -s -o NUL -w "%{http_code}" "$BaseUrl/questions"
Assert-True ($questionsStatus -eq "200") "Questions page unavailable, status: $questionsStatus"

Write-Host ""
Write-Host "PASS: startup self-check"
Write-Host "- table_count: $tableCount"
Write-Host "- t_question: $tqCount"
Write-Host "- sys_question: $sqCount"
Write-Host "- Oliveira_role: $oliveiraRole"
Write-Host "- login_check: Oliveira/testuser passed"
Write-Host "- page_check: /questions = 200"
