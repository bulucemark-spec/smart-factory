. (Join-Path $PSScriptRoot "TestKit.ps1")

$tok = $null
for ($i=0; $i -lt 20; $i++) {
    try { $tok = Login "admin" "admin123"; if ($tok) { break } } catch {}
    Start-Sleep -Seconds 3
}
if (-not $tok) { throw "system-service 未就绪" }

$Global:Results = [System.Collections.Generic.List[object]]::new()
foreach ($o in (Load-Results)) {
    if ($o.name -ne "边界-非法JSON报文→1004") { [void]$Global:Results.Add($o) }
}
Save-Results

$r = Api "POST" "/api/system/auth/login" "{broken json" $null
Add-Result "边界-非法JSON报文→1004" ($r.code -eq 1004) "code=$($r.code)"
Show-Stats
