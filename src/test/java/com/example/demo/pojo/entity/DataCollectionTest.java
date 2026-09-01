package com.example.demo.pojo.entity;

import com.example.demo.pojo.dto.mapvo.DataField;
import com.example.demo.pojo.dto.remark.FillBlankRemarkDTO;
import com.example.demo.util.DataCollectionDataUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataCollectionTest {

    @Test
    void parseCorrectAnswerMapParsesStoredJson() {
        DataCollection dataCollection = new DataCollection();
        dataCollection.setCorrectAnswer("{\"Uab\":\"220\",\"I1\":\"0.5\"}");

        Map<String, String> result = dataCollection.parseCorrectAnswerMap();

        assertEquals(Map.of("Uab", "220", "I1", "0.5"), result);
    }

    @Test
    void parseCorrectAnswerMapRejectsInvalidJson() {
        DataCollection dataCollection = new DataCollection();
        dataCollection.setCorrectAnswer("not-json");

        assertThrows(IllegalArgumentException.class, dataCollection::parseCorrectAnswerMap);
    }

    @Test
    void resolveFillBlankRemarkMergesCorrectAnswerValues() throws Exception {
        DataCollection dataCollection = new DataCollection();
        dataCollection.setRemark("{\"fillBlanks\":[{\"fieldName\":\"Uab\",\"value\":\"\"}]}");
        dataCollection.setCorrectAnswer("{\"Uab\":\"220\"}");

        FillBlankRemarkDTO result = dataCollection.resolveFillBlankRemark(true);

        assertEquals("220", result.getFillBlanks().get(0).getMin());
        assertEquals("220", result.getFillBlanks().get(0).getMax());

        String responseJson = new ObjectMapper().writeValueAsString(result);
        assertTrue(responseJson.contains("\"min\":\"220\""));
        assertTrue(responseJson.contains("\"max\":\"220\""));
        assertFalse(responseJson.contains("\"value\""));
    }

    @Test
    void resolveFillBlankRemarkExpandsRangeAnswer() throws Exception {
        DataCollection dataCollection = new DataCollection();
        dataCollection.setRemark("{\"fillBlanks\":[{\"fieldName\":\"Uab\",\"value\":\"\"}]}");
        dataCollection.setCorrectAnswer("{\"Uab\":\"RANGE|210.0|230.0\"}");

        FillBlankRemarkDTO result = dataCollection.resolveFillBlankRemark(true);

        assertEquals("210.0", result.getFillBlanks().get(0).getMin());
        assertEquals("230.0", result.getFillBlanks().get(0).getMax());

        String responseJson = new ObjectMapper().writeValueAsString(result);
        assertTrue(responseJson.contains("\"min\":\"210.0\""));
        assertTrue(responseJson.contains("\"max\":\"230.0\""));
        assertFalse(responseJson.contains("\"value\""));
    }

    @Test
    void rangeAnswerIsStoredInCorrectAnswerButNotRemark() {
        DataField field = new DataField();
        field.setFieldName("Uab");
        field.setMin("210.0");
        field.setMax("230.0");

        assertEquals("RANGE|210.0|230.0", DataField.toMap(java.util.List.of(field)).get("Uab"));

        String remarkJson = DataCollectionDataUtil.convertFillBlanksToJson(java.util.List.of(field));
        assertFalse(remarkJson.contains("\"min\""));
        assertFalse(remarkJson.contains("\"max\""));
    }

    @Test
    void rangeAnswerUsesInclusiveBoundsWithoutTolerance() {
        String range = "RANGE|210.0|230.0";

        assertTrue(DataField.isAnswerWithinRange("210.0", range));
        assertTrue(DataField.isAnswerWithinRange("220", range));
        assertTrue(DataField.isAnswerWithinRange("230.0", range));
        assertFalse(DataField.isAnswerWithinRange("209.99", range));
        assertFalse(DataField.isAnswerWithinRange("230.01", range));
    }

    @Test
    void resolveCorrectAnswerRangesReturnsStructuredMinAndMax() throws Exception {
        DataCollection dataCollection = new DataCollection();
        dataCollection.setCorrectAnswer("{\"0-0\":\"RANGE|3.3|3.7\",\"0-1\":\"60\"}");

        assertEquals("3.3", dataCollection.resolveCorrectAnswerRanges().get("0-0").getMin());
        assertEquals("3.7", dataCollection.resolveCorrectAnswerRanges().get("0-0").getMax());
        assertEquals("60", dataCollection.resolveCorrectAnswerRanges().get("0-1").getMin());
        assertEquals("60", dataCollection.resolveCorrectAnswerRanges().get("0-1").getMax());

        String responseJson = new ObjectMapper()
                .writeValueAsString(dataCollection.resolveCorrectAnswerRanges());
        assertTrue(responseJson.contains("\"0-0\":{\"min\":\"3.3\",\"max\":\"3.7\"}"));
        assertFalse(responseJson.contains("RANGE|"));
    }

    @Test
    void entitySerializationDoesNotExposeStoredCorrectAnswer() throws Exception {
        DataCollection dataCollection = new DataCollection();
        dataCollection.setCorrectAnswer("{\"Uab\":\"RANGE|210|230\"}");

        String json = new ObjectMapper().writeValueAsString(dataCollection);

        assertFalse(json.contains("correctAnswer"));
        assertFalse(json.contains("RANGE|"));
    }

    @Test
    void malformedInternalRangeIsRejectedInsteadOfExposed() {
        DataCollection dataCollection = new DataCollection();
        dataCollection.setCorrectAnswer("{\"Uab\":\"RANGE|210\"}");

        assertThrows(IllegalArgumentException.class, dataCollection::resolveCorrectAnswerRanges);
    }

    @Test
    void hiddenAnswerRemarkContainsStructureOnlyEvenForLegacyValue() throws Exception {
        DataCollection dataCollection = new DataCollection();
        dataCollection.setRemark("{\"fillBlanks\":[{\"fieldName\":\"Uab\",\"value\":\"220\"}]}");
        dataCollection.setCorrectAnswer("{\"Uab\":\"RANGE|210|230\"}");

        String responseJson = new ObjectMapper()
                .writeValueAsString(dataCollection.resolveFillBlankRemark(false));

        assertTrue(responseJson.contains("\"fieldName\":\"Uab\""));
        assertFalse(responseJson.contains("\"value\""));
        assertFalse(responseJson.contains("\"min\""));
        assertFalse(responseJson.contains("\"max\""));
        assertFalse(responseJson.contains("RANGE|"));
    }

    @Test
    void invalidNumericOrReversedInternalRangesAreRejected() {
        DataCollection invalidNumber = new DataCollection();
        invalidNumber.setCorrectAnswer("{\"Uab\":\"RANGE|abc|230\"}");
        DataCollection reversed = new DataCollection();
        reversed.setCorrectAnswer("{\"Uab\":\"RANGE|230|210\"}");

        assertThrows(IllegalArgumentException.class, invalidNumber::resolveCorrectAnswerRanges);
        assertThrows(IllegalArgumentException.class, reversed::resolveCorrectAnswerRanges);
    }

    @Test
    void legacyExactAnswerUsesTrueMinEqualsMaxComparison() {
        assertTrue(DataField.isFillBlankAnswerCorrect("220.0", "220"));
        assertFalse(DataField.isFillBlankAnswerCorrect("220.00009", "220"));
        assertTrue(DataField.isFillBlankAnswerCorrect("OK", "ok"));
    }

    @Test
    void tableAnswerWithRangePrefixRemainsAnExactString() {
        DataCollection dataCollection = new DataCollection();
        dataCollection.setType(2L);
        dataCollection.setCorrectAnswer("{\"0-0\":\"RANGE|3|5\"}");

        assertEquals("RANGE|3|5", dataCollection.resolveCorrectAnswerRanges().get("0-0").getMin());
        assertEquals("RANGE|3|5", dataCollection.resolveCorrectAnswerRanges().get("0-0").getMax());
    }
}
