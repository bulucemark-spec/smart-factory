package com.smartfactory.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.util.SeqNo;
import com.smartfactory.common.result.Result;
import com.smartfactory.wms.entity.Asn;
import com.smartfactory.wms.entity.AsnItem;
import com.smartfactory.wms.mapper.AsnItemMapper;
import com.smartfactory.wms.mapper.AsnMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wms/asns")
@RequiredArgsConstructor
public class AsnController {

    private final AsnMapper asnMapper;
    private final AsnItemMapper itemMapper;

    @GetMapping
    public Result<List<Asn>> list() {
        return Result.ok(asnMapper.selectList(
                new LambdaQueryWrapper<Asn>().orderByDesc(Asn::getId)));
    }

    @PostMapping
    public Result<String> create(@RequestBody Asn asn) {
        asn.setAsnNo(SeqNo.next("ASN"));
        asn.setStatus("PLANNED");
        asnMapper.insert(asn);
        if (asn.getItems() != null) {
            for (AsnItem item : asn.getItems()) {
                item.setId(null);
                item.setAsnId(asn.getId());
                itemMapper.insert(item);
            }
        }
        return Result.ok(asn.getAsnNo());
    }

    @GetMapping("/{id}/detail")
    public Result<Asn> detail(@PathVariable Long id) {
        Asn asn = asnMapper.selectById(id);
        if (asn != null) {
            asn.setItems(itemMapper.selectList(
                    new LambdaQueryWrapper<AsnItem>().eq(AsnItem::getAsnId, id)));
        }
        return Result.ok(asn);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        asnMapper.deleteById(id);
        return Result.ok();
    }
}
