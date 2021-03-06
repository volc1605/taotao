package com.taotao.search.pojo;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.beans.Field;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
/*
 * 忽略为知的字段,因为以下字段是为写入到solr用的，用jackson反序列化时需要，反序列化以下字段即可，不需要加入到solr的字段我们需要忽略.
 * 否则在反序列化时会报错
 * com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field "created" (class cn.itcast.solrj.pojo.Item), not marked as ignorable (8 known properties: "status", "title", "price", "id", "updated", "image", "sellPoint", "cid"])
 */

public class Item {

    @Field("id")
    private Long id;

    @Field("title")
    private String title;

    @Field("sellPoint")
    private String sellPoint;

    @Field("price")
    private Long price;

    @Field("image")
    private String image;

    @Field("cid")
    private Long cid;

    @Field("status")
    private Integer status;

    @Field("updated")
    private Long updated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSellPoint() {
        return sellPoint;
    }

    public void setSellPoint(String sellPoint) {
        this.sellPoint = sellPoint;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public String[] getImages() {
        return StringUtils.split(this.getImage(), ',');
    }

    @Override
    public String toString() {
        return "Item [id=" + id + ", title=" + title + ", sellPoint=" + sellPoint + ", price=" + price
                + ", image=" + image + ", cid=" + cid + ", status=" + status + ", updated=" + updated + "]";
    }

}
