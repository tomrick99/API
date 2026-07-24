package org.example.file_api.dto;

import java.util.List;

// 用于返回分页结果
public class MaterialPageRespDTO {

    private List<MaterialRespDTO> records;
    private long total;
    private long page;
    private long pageSize;

    public List<MaterialRespDTO> getRecords() {
        return records;
    }

    public void setRecords(List<MaterialRespDTO> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
