package com.neu.mtinv.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MonitorService {
    List<Map> getCatalog();

    Map<String, Object> getResult(String id, String resulFile) throws IOException;

    byte[] getImage(String imageUrl) throws IOException;
}
