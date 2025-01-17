package no.asf.formidling.client.ws.client;

import no.altinn.correspondenceexternalec.AttachmentBEV2;
import no.altinn.correspondenceexternalec.CorrespondenceForEndUserSystemV2;
import no.altinn.intermediaryinboundec.Attachment;
import no.altinn.intermediaryinboundec.ReceiptExternalBE;
import no.altinn.receiptexternalec.*;
import no.asf.formidling.client.config.EC2ClientConfig;
import no.asf.formidling.client.vo.SecurityCredentials;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@ContextConfiguration(classes = {EC2ClientConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class IntermediaryInboundTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private SecurityCredentials avgiverCredentials;

    private IntermediaryInboundExternalEC2Client inboundExternalEC2Client;

    private ReceiptEC2Client receiptEC2Client;
    private ReporteeElementListEC2Client reporteeElementListEC2Client;

    private CorrespondenceEC2Client correspondenceEC2Client;
    private static int LANGUAGEID_NB = 1044;

    private static String TMP_DIR="c:\\temp\\Skattekort\\";

    @Before
    public void setup() {
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new ClassPathResource("properties/avgiver-properties.xml").getInputStream());
        } catch (IOException e) {
            fail("Unable to load properties");
        }
        log.info("Getting receipt for\n" +
                "Avgiver = " + properties.getProperty("avgiver.entityusername"));

        avgiverCredentials = new SecurityCredentials(properties.getProperty("avgiver.virksomhetsbruker"),
                properties.getProperty("avgiver.virksomhetsbrukerpassord"),
                properties.getProperty("avgiver.entity"),
                properties.getProperty("avgiver.certificate"),
                properties.getProperty("avgiver.entitypassword"),
                properties.getProperty("avgiver.alias"));

        inboundExternalEC2Client = new IntermediaryInboundExternalEC2Client(avgiverCredentials);
        receiptEC2Client = new ReceiptEC2Client(avgiverCredentials);
        reporteeElementListEC2Client = new ReporteeElementListEC2Client(avgiverCredentials);
        correspondenceEC2Client = new CorrespondenceEC2Client(avgiverCredentials);
    }

    @Test
    public void sendFormTaskTest() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("FormTask.xml");
        String formData = FileUtils.readFileToString(classPathResource.getFile(), "utf-8");
        String externalShipmentReference = UUID.randomUUID().toString();
        String endUserSystemReference = UUID.randomUUID().toString();
        String reportee = "810514442";
        String serviceCode = "3928";
        int serviceEdition = 1;
        String dataFormatId = "3881";
        int dataFormatVersion = 34642;
        ReceiptExternalBE receiptExternalBE = inboundExternalEC2Client.sendFormTaskShipment(reportee, externalShipmentReference, serviceCode,
                serviceEdition, dataFormatId, dataFormatVersion, endUserSystemReference, formData, null);
        assertThat(receiptExternalBE.getReceiptStatusCode().value(), is("OK"));
    }

    @Test
    public void sendFormTaskSkattekortTest() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("FormTask.xml");
        String formData = FileUtils.readFileToString(classPathResource.getFile(), "utf-8");
        String externalShipmentReference = UUID.randomUUID().toString();
        String endUserSystemReference = UUID.randomUUID().toString();
        String reportee = "810514442";
        String serviceCode = "3103";
        int serviceEdition = 180925;
        String dataFormatId = "3881";
        int dataFormatVersion = 34642;
        ReceiptExternalBE receiptExternalBE = inboundExternalEC2Client.sendFormTaskShipment(reportee, externalShipmentReference, serviceCode,
                serviceEdition, dataFormatId, dataFormatVersion, endUserSystemReference, formData, null);
        assertThat(receiptExternalBE.getReceiptStatusCode().value(), is("OK"));


    }


}
