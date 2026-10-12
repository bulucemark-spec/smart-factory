# 扫描 4 库受双重编码影响的文本列
$colsSql = "SELECT TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA IN ('smart_system','smart_wms','smart_mes','smart_qms') AND DATA_TYPE IN ('varchar','char','text','tinytext','mediumtext','longtext') ORDER BY TABLE_SCHEMA,TABLE_NAME;"
$cols = docker exec sf-mysql sh -c "mysql -uroot -psmart123 -N -B -e \"$colsSql\" 2>/dev/null"
$log = [System.Collections.Generic.List[string]]::new()
$fixSql = [System.Collections.Generic.List[string]]::new()
foreach ($r in ($cols -split "`n")) {
    $r = $r.Trim()
    if (-not $r) { continue }
    $f = $r -split "`t"
    if ($f.Count -lt 3) { continue }
    $db, $tb, $col = $f[0], $f[1], $f[2]
    $cntSql = "SELECT COUNT(*) FROM \`"$db\`".\`"$tb\`" WHERE HEX(\`"$col\`") LIKE '%C3%' OR HEX(\`"$col\`") LIKE '%C2%';"
    $cntOut = docker exec sf-mysql sh -c "mysql -uroot -psmart123 -N -B -e \"$cntSql\" 2>/dev/null"
    $cnt = ($cntOut -split "`n" | Where-Object { $_.Trim() } | Select-Object -First 1).Trim()
    if (-not $cnt) { $cnt = 0 }
    if ([int]$cnt -gt 0) {
        $fixSql.Add("UPDATE \`"$db\`".\`"$tb\`" SET \`"$col\`" = CONVERT(CAST(CONVERT(\`"$col\`" USING latin1) AS BINARY) USING utf8mb4) WHERE HEX(\`"$col\`") LIKE '%C3%' OR HEX(\`"$col\`") LIKE '%C2%';")
        $log.Add("$db.$tb.$col -> $cnt 行需修复")
    }
}
[IO.File]::WriteAllLines((Join-Path $PSScriptRoot "fix-mojibake.sql"), $fixSql, (New-Object Text.UTF8Encoding($false)))
[IO.File]::WriteAllLines((Join-Path $PSScriptRoot "fix-log.txt"), $log, (New-Object Text.UTF8Encoding($true)))
Write-Output ("待修复列数: " + $fixSql.Count)
