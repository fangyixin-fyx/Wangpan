package com.wangpan.entity.query;

public class BaseQuery {
    private SimplePage simplePage;
    private Integer pageNo;
    private Integer pageSize;
    private String orderBy;

    public SimplePage getSimplePage() {
        return this.simplePage;
    }
    public Integer getPageNo(){
        return this.pageNo;
    }
    public Integer getPageSize() {
        return this.pageSize;
    }
    public String getOrderBy() {
        return this.orderBy;
    }

    public void setSimplePage(SimplePage simplePage) {
        this.simplePage=simplePage;
    }
    public void setPageNo(Integer pageNo){
        this.pageNo=pageNo;
    }
    public void setPageSize(Integer pageSize) {
        this.pageSize=pageSize;
    }
    public void setOrderBy(String orderBy) {
        this.orderBy=orderBy;
    }
}
