param(
    [string]$MysqlContainer = "codequest-mysql",
    [string]$SqlFile = ".\scripts\recover_codequest_db.sql"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $SqlFile)) {
    throw "SQL file not found: $SqlFile"
}

docker cp $SqlFile "${MysqlContainer}:/tmp/recover_codequest_db.sql"
docker exec $MysqlContainer sh -lc "mysql -uroot -proot < /tmp/recover_codequest_db.sql"
Write-Host "Recover SQL executed successfully."