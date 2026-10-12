# 智能工厂 全方位测试框架（被各阶段测试脚本 dot-source）
$Global:Base = "http://localhost:8090"
$Global:ResFile = Join-Path $PSScriptRoot "results.json"

function Load-Results {
    if (Test-Path $Global:ResFile) {
        $j = [IO.File]::ReadAllText($Global:ResFile,[Text.Encoding]::UTF8) | ConvertFrom-Json
        if ($j -is [System.Array]) { ,$j } else { ,@($j) }
    } else { ,@() }
}
function Save-Results {
    $arr = @($Global:Results.ToArray())
    $json = ConvertTo-Json -InputObject $arr -Depth 8
    if ($arr.Count -eq 1) { $json = "[" + $json + "]" }
    [IO.File]::WriteAllText($Global:ResFile, $json, (New-Object Text.UTF8Encoding($true)))
}

$Global:Results = [System.Collections.Generic.List[object]]::new()
foreach ($o in (Load-Results)) { [void]$Global:Results.Add($o) }

function Add-Result($name, $pass, $detail) {
    [void]$Global:Results.Add([pscustomobject]@{ name = "$name"; pass = [bool]$pass; detail = "$detail" })
    Save-Results
    $tag = if ($pass) { "PASS" } else { "FAIL" }
    Write-Host "[$tag] $name $detail"
}

# 统一调用：Api METHOD /path BODY(对象或字符串或null) TOKEN
function Api($method, $path, $body, $token) {
    $url = "$Global:Base$path"
    $ca = @("-s", "--noproxy", "*", "-X", $method, $url, "-w", "`n%{http_code}")
    $tmp = $null
    if ($null -ne $body) {
        $tmp = Join-Path $env:TEMP ("api_" + [guid]::NewGuid().ToString("N") + ".json")
        $json = if ($body -is [string]) { $body } else { ($body | ConvertTo-Json -Depth 10 -Compress) }
        [IO.File]::WriteAllText($tmp, $json, (New-Object Text.UTF8Encoding($false)))
        $ca += @("-H", "Content-Type: application/json", "--data-binary", "@$tmp")
    }
    if ($token) { $ca += @("-H", "Authorization: Bearer $token") }
    $out = & curl.exe @ca
    if ($tmp) { Remove-Item $tmp -ErrorAction SilentlyContinue }
    $lines = ($out -join "`n") -split "`n"
    $http = $lines[-1].Trim()
    $raw = ($lines[0..([Math]::Max(0,$lines.Count - 2))] -join "`n").Trim()
    $j = $null; try { $j = $raw | ConvertFrom-Json } catch {}
    [pscustomobject]@{ http = $http; code = if ($j) { $j.code } else { $null }; message = if ($j) { $j.message } else { $null }; data = if ($j) { $j.data } else { $null }; raw = $raw }
}

function Login($u, $p) {
    (Api "POST" "/api/system/auth/login" @{ username = $u; password = $p } $null).data.accessToken
}

function Show-Stats {
    $total = $Global:Results.Count
    $pass = @($Global:Results | Where-Object { $_.pass }).Count
    Write-Host "----- 累计: $total 用例, PASS $pass, FAIL $($total-$pass) -----"
}
