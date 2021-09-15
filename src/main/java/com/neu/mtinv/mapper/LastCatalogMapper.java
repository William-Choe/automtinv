package com.neu.mtinv.mapper;

import org.springframework.stereotype.Component;

@Component
public interface LastCatalogMapper {
    String getEqTime();
    
    void updateEqTime(String eqTime);
}
