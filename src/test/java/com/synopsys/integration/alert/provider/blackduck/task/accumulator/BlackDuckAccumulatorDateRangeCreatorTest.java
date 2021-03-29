package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.rest.RestConstants;

public class BlackDuckAccumulatorDateRangeCreatorTest {
    @Test
    public void testCreateDateRange() {
        BlackDuckAccumulatorDateRangeCreator dateRangeCreator = createDateRangeCreator(null);
        DateRange dateRange = dateRangeCreator.createDateRange("Task");
        assertNotNull(dateRange);
        ZonedDateTime startTime = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneOffset.UTC);
        ZonedDateTime endTime = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneOffset.UTC);
        assertNotEquals(dateRange.getStart(), dateRange.getEnd());
        ZonedDateTime expectedStartTime = endTime.minusMinutes(1);
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void createDateRangeParseExceptionTest() {
        BlackDuckAccumulatorDateRangeCreator dateRangeCreator = createDateRangeCreator("Not a date");

        DateRange dateRange = dateRangeCreator.createDateRange("Task");
        assertNotNull(dateRange);
        assertEquals(dateRange.getStart(), dateRange.getEnd());
    }

    @Test
    public void testCreateDateRangeWithExistingFile() {
        OffsetDateTime expectedStartDate = ZonedDateTime.now(ZoneOffset.UTC)
                                               .withSecond(0)
                                               .withNano(0)
                                               .minusMinutes(5)
                                               .toOffsetDateTime();
        String expectedStartDateString = DateUtils.formatDate(expectedStartDate, RestConstants.JSON_DATE_FORMAT);

        BlackDuckAccumulatorDateRangeCreator dateRangeCreator = createDateRangeCreator(expectedStartDateString);
        DateRange dateRange = dateRangeCreator.createDateRange("Task");
        assertNotNull(dateRange);
        OffsetDateTime actualStartDate = dateRange.getStart();
        OffsetDateTime actualEndDate = dateRange.getEnd();
        assertEquals(expectedStartDate, actualStartDate);
        assertNotEquals(actualStartDate, actualEndDate);
    }

    private BlackDuckAccumulatorDateRangeCreator createDateRangeCreator(String expectedDate) {
        ProviderTaskPropertiesAccessor mockTaskPropsAccessor = Mockito.mock(ProviderTaskPropertiesAccessor.class);
        Mockito.when(mockTaskPropsAccessor.getTaskProperty(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.ofNullable(expectedDate));
        return new BlackDuckAccumulatorDateRangeCreator(mockTaskPropsAccessor);
    }

}
