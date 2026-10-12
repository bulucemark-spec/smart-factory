package com.smartfactory.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfactory.wms.entity.Inventory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface InventoryMapper extends BaseMapper<Inventory> {

  @Select("SELECT status, COALESCE(SUM(qty),0) AS qty, COUNT(*) AS cnt " +
          "FROM wms_inventory GROUP BY status")
  List<Map<String, Object>> statusSummary();

    /** 条件扣减，防止超扣；影响行数 0 表示库存不足 */
    @Update("UPDATE wms_inventory SET qty = qty - #{qty}, update_time = NOW() " +
            "WHERE id = #{id} AND qty >= #{qty} AND status = 'QUALIFIED'")
    int deductQty(@Param("id") Long id, @Param("qty") BigDecimal qty);

    /** 通用条件扣减（不限状态，用于待检移位/退货） */
    @Update("UPDATE wms_inventory SET qty = qty - #{qty}, update_time = NOW() " +
            "WHERE id = #{id} AND qty >= #{qty}")
    int deductAny(@Param("id") Long id, @Param("qty") BigDecimal qty);

    @Update("UPDATE wms_inventory SET qty = qty + #{qty}, update_time = NOW() WHERE id = #{id}")
    int addQty(@Param("id") Long id, @Param("qty") BigDecimal qty);

    @Update("UPDATE wms_inventory SET status = #{toStatus}, update_time = NOW() WHERE id = #{id}")
    int changeStatus(@Param("id") Long id, @Param("toStatus") String toStatus);
}
