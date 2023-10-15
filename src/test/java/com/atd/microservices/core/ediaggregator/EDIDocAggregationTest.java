package com.atd.microservices.core.ediaggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.berryworks.edireader.json.toedi.JsonMultipleToEdi;

public class EDIDocAggregationTest {
	@Test
    public void testAppendJson() throws Exception {
		List<String> jsonDocuments = new ArrayList<>();
        jsonDocuments.add(JSON_1);
        jsonDocuments.add(JSON_1.replace("000042460", "000042461"));
        Writer writer = new StringWriter();
        JsonMultipleToEdi jsonMultipleToEdi = new JsonMultipleToEdi();
        jsonMultipleToEdi.asEdi(jsonDocuments, writer);
        assertEquals("" +
                "ISA*00*          *00*          *ZZ*04000          *ZZ*58401          *040714*1003*U*00204*000038449*1*P*:$\n" +
                "GS*AG*04000*58401*040714*1003*38327*X*002040CHRY$\n" +
                "ST*824*000042460$\n" +
                "BGN*11*07141005162*040714*1003$\n" +
                "N1*SU**92*58401O$\n" +
                "SE*4*000042460$\n" +
                "ST*824*000042461$\n" +
                "BGN*11*07141005162*040714*1003$\n" +
                "N1*SU**92*58401O$\n" +
                "SE*4*000042461$\n" +
                "GE*2*38327$\n" +
                "IEA*1*000038449$\n", writer.toString());
    }
	
	private static String JSON_1 = "" +
            "{\n" +
            "  \"interchanges\": [\n" +
            "    {\n" +
            "      \"ISA_01_AuthorizationQualifier\": \"00\",\n" +
            "      \"ISA_02_AuthorizationInformation\": \"          \",\n" +
            "      \"ISA_03_SecurityQualifier\": \"00\",\n" +
            "      \"ISA_04_SecurityInformation\": \"          \",\n" +
            "      \"ISA_05_SenderQualifier\": \"ZZ\",\n" +
            "      \"ISA_06_SenderId\": \"04000          \",\n" +
            "      \"ISA_07_ReceiverQualifier\": \"ZZ\",\n" +
            "      \"ISA_08_ReceiverId\": \"58401          \",\n" +
            "      \"ISA_09_Date\": \"040714\",\n" +
            "      \"ISA_10_Time\": \"1003\",\n" +
            "      \"ISA_11_StandardsId\": \"U\",\n" +
            "      \"ISA_12_Version\": \"00204\",\n" +
            "      \"ISA_13_InterchangeControlNumber\": \"000038449\",\n" +
            "      \"ISA_14_AcknowledgmentRequested\": \"1\",\n" +
            "      \"ISA_15_TestIndicator\": \"P\",\n" +
            "      \"functional_groups\": [\n" +
            "        {\n" +
            "          \"GS_01_FunctionalIdentifierCode\": \"AG\",\n" +
            "          \"GS_02_ApplicationSenderCode\": \"04000\",\n" +
            "          \"GS_03_ApplicationReceiverCode\": \"58401\",\n" +
            "          \"GS_04_Date\": \"040714\",\n" +
            "          \"GS_05_Time\": \"1003\",\n" +
            "          \"GS_06_GroupControlNumber\": \"38327\",\n" +
            "          \"GS_07_ResponsibleAgencyCode\": \"X\",\n" +
            "          \"GS_08_Version\": \"002040CHRY\",\n" +
            "          \"transactions\": [\n" +
            "            {\n" +
            "              \"ST_01_TransactionSetIdentifierCode\": \"824\",\n" +
            "              \"ST_02_TransactionSetControlNumber\": \"000042460\",\n" +
            "              \"segments\": [\n" +
            "                {\n" +
            "                  \"BGN_01\": \"11\",\n" +
            "                  \"BGN_02\": \"07141005162\",\n" +
            "                  \"BGN_03\": \"040714\",\n" +
            "                  \"BGN_04\": \"1003\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"N1-N1_loop\": [\n" +
            "                    {\n" +
            "                      \"N1_01\": \"SU\",\n" +
            "                      \"N1_03\": \"92\",\n" +
            "                      \"N1_04\": \"58401O\"\n" +
            "                    }\n" +
            "                  ]\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

}
