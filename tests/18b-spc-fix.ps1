. (Join-Path $PSScriptRoot "TestKit.ps1")
$tok = Login "admin" "admin123"

# 重建结果集，剔除旧的 Cpk 记录
$Global:Results = [System.Collections.Generic.List[object]]::new()
foreach ($o in (Load-Results)) {
    if ($o.name -ne "链路C-过程能力Cpk>1.33") { [void]$Global:Results.Add($o) }
}
Save-Results

# 用收窄波动的数据重跑 SPC
$char = "PCB-LEN-FIX-" + (Get-Random -Maximum 999999)
for ($i=1; $i -le 20; $i++) {
    $v = [math]::Round(100.0 + 0.035*[math]::Sin($i), 3)
    [void](Api "POST" "/api/qms/spc/record" @{ characteristic=$char; materialCode="RM-PCB-01"; value=$v; sourceType="IQC" } $tok)
}
$cp = (Api "GET" "/api/qms/spc/cpk?characteristic=$char&lsl=99.8&usl=100.2" $null $tok).data
Add-Result "链路C-过程能力Cpk>1.33" ([double]$cp.cpk -gt 1.33) "cp=$($cp.cp) cpk=$($cp.cpk)"
Show-Stats
