# 读取列清单，为全部文本列生成带保护条件的修复 SQL
$cols = Get-Content (Join-Path $PSScriptRoot "cols.out")
$fixSql = [System.Collections.Generic.List[string]]::new()
foreach ($r in $cols) {
    $r = $r.Trim()
    if (-not $r) { continue }
    $f = $r -split "`t"
    if ($f.Count -lt 3) { continue }
    $db, $tb, $col = $f[0], $f[1], $f[2]
    $fixSql.Add("UPDATE ``$db``.``$tb`` SET ``$col`` = CONVERT(CAST(CONVERT(``$col`` USING latin1) AS BINARY) USING utf8mb4) WHERE HEX(``$col``) LIKE '%C3%' OR HEX(``$col``) LIKE '%C2%';")
}
[IO.File]::WriteAllLines((Join-Path $PSScriptRoot "fix-mojibake.sql"), $fixSql, (New-Object Text.UTF8Encoding($false)))
Write-Output ("生成修复语句: " + $fixSql.Count)
