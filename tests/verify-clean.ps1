# 字节边界正确检测：排除 HEX 文本误报
$total = 0
cmd /c "docker exec -i sf-mysql mysql -uroot -psmart123 -N -B < E:\code\smart-factory\tests\cols3.sql > E:\code\smart-factory\tests\cols4.out 2>nul"
foreach ($r in (Get-Content (Join-Path $PSScriptRoot "cols4.out"))) {
    $r = $r.Trim(); if (-not $r) { continue }
    $f = $r -split '\.'; if ($f.Count -lt 3) { continue }
    $db, $tb, $col = $f[0], $f[1], $f[2]
    # 跳过备份表
    if ($tb -like '*_bak_*') { continue }
    $sql = "SELECT COUNT(*) FROM ``$db``.``$tb`` WHERE HEX(``$col``) REGEXP '^(?:[0-9A-F]{2})*(C2|C3)[0-9A-F]{2}';"
    $tmp = Join-Path $env:TEMP ("ck_" + [guid]::NewGuid().ToString("N") + ".sql")
    [IO.File]::WriteAllText($tmp, $sql, (New-Object Text.UTF8Encoding($false)))
    $out = $tmp + ".out"
    cmd /c "docker exec -i sf-mysql mysql -uroot -psmart123 -N -B < $tmp > $out 2>nul" | Out-Null
    $c = (Get-Content $out -ErrorAction SilentlyContinue | Select-Object -First 1).Trim()
    Remove-Item $tmp, $out -ErrorAction SilentlyContinue
    if ($c) {
        $n = [int]$c
        if ($n -gt 0) { Write-Output ("残留: {0} -> {1}" -f $r, $n); $total += $n }
    }
}
Write-Output ("业务表残留总数: " + $total)
