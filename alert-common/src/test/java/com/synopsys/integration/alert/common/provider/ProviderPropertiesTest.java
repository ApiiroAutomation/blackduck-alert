package com.synopsys.integration.alert.common.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;

public class ProviderPropertiesTest {
    @Test
    public void getConfigIdTest() {
        Long id = 23L;
        FieldUtility fieldUtility = new FieldUtility(Map.of());
        ProviderProperties properties = new ProviderProperties(id, fieldUtility) {};
        assertEquals(id, properties.getConfigId());
    }

}
