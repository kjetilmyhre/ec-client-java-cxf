package no.asf.formidling.client.ws.client;

import no.altinn.correspondenceexternalec.AttachmentBEV2;
import no.altinn.correspondenceexternalec.CorrespondenceForEndUserSystemV2;
import no.altinn.intermediaryinboundec.Attachment;
import no.altinn.intermediaryinboundec.ReceiptExternalBE;
import no.altinn.receiptexternalec.Receipt;
import no.altinn.receiptexternalec.Reference2;
import no.altinn.receiptexternalec.ReferenceType;
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
public class CoreSuiteTest {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private SecurityCredentials avgiverCredentials;

    private IntermediaryInboundExternalEC2Client inboundExternalEC2Client;

    private ReceiptEC2Client receiptEC2Client;
    private ReporteeElementListEC2Client reporteeElementListEC2Client;

    private CorrespondenceEC2Client correspondenceEC2Client;
    private static int LANGUAGEID_NB = 1044;

    private static String OUTPUT_DIR="c:\\temp\\";
    private static String OUTPUT_DIR_SKATTEKORT = OUTPUT_DIR+"Skattekort\\";
    private static String OUTPUT_DIR_AMELDING = OUTPUT_DIR+"Skattekort\\";

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
    public void getTaxCardIntegrationTest() throws Exception {
        ClassPathResource requestTaxCardResource = new ClassPathResource("RequestTaxCard.xml");
        ClassPathResource requestTaxCardPayload = new ClassPathResource("RequestTaxCardPayload.zip");
        String formData = FileUtils.readFileToString(requestTaxCardResource.getFile(), "utf-8");
        byte[] payloadData = FileUtils.readFileToByteArray(requestTaxCardPayload.getFile());
        String externalShipmentReference = UUID.randomUUID().toString();
        String endUserSystemReference = UUID.randomUUID().toString();
        String reportee = "810514442";
        String serviceCode = "3103";
        int serviceEdition = 180925;
        String dataFormatId = "1548";
        int dataFormatVersion = 12174;
        Attachment attachment = inboundExternalEC2Client.getEmptyAttachment();
        attachment.setAttachementData(payloadData);
        attachment.setEndUserSystemReference(endUserSystemReference);
        attachment.setEncrypted(false);
        attachment.setName("Skattekort");
        attachment.setFileName("RequestTaxCardPayload.zip");
        attachment.setParentReference(null);
        List<Attachment> attachments = new LinkedList<>();
        attachments.add(attachment);
        ReceiptExternalBE receiptExternalBE = inboundExternalEC2Client.sendFormTaskShipment(reportee, externalShipmentReference, serviceCode,
                serviceEdition, dataFormatId, dataFormatVersion, endUserSystemReference, formData, attachments);
        assertThat(receiptExternalBE.getReceiptStatusCode().value(), is("OK"));

        int receiptId = receiptExternalBE.getReceiptId();


        Integer receiverReference = null;
        while(receiverReference == null ) {
            Receipt receipt = receiptEC2Client.getReceipt(receiptId, null);
            assertThat(receipt, notNullValue());
            log.info("Receipt received: " + receipt.getReceiptId() + receipt.getReceiptText().getValue());
            receiverReference = findReceiverReference(receipt);
            Thread.sleep(1000);
        }

        CorrespondenceForEndUserSystemV2 correspondence = correspondenceEC2Client.getCorrespondenceForEndUserSystemsEC(receiverReference, LANGUAGEID_NB);
        log.info(correspondence.toString());
        List<AttachmentBEV2> returnAttachmentsFromTax = correspondence.getCorrespondenceAttachments().getValue().getAttachmentBEV2();
        for (AttachmentBEV2 a : returnAttachmentsFromTax) {
            byte[] attachmentdata = a.getAttachmentData().getValue();
            String attachmentName = "TaxCardsReturnedFromTax"+System.currentTimeMillis()+".zip";
            FileUtils.writeByteArrayToFile(new File(OUTPUT_DIR_SKATTEKORT+attachmentName), attachmentdata);
        }
    }

    @Test
    public void sendAmeldingIntegrationTest() throws Exception {
        ClassPathResource requestTaxCardResource = new ClassPathResource("SendAmeldingForm.xml");
        ClassPathResource requestTaxCardPayload = new ClassPathResource("PayloadAmelding.zip");
        String formData = FileUtils.readFileToString(requestTaxCardResource.getFile(), "utf-8");
        byte[] payloadData = FileUtils.readFileToByteArray(requestTaxCardPayload.getFile());
        String externalShipmentReference = UUID.randomUUID().toString();
        String endUserSystemReference = UUID.randomUUID().toString();
        String reportee = "810514442";
        String serviceCode = "3357";
        int serviceEdition = 140318;
        String dataFormatId = "4166";
        int dataFormatVersion = 35895;
        Attachment attachment = inboundExternalEC2Client.getEmptyAttachment();
        attachment.setAttachementData(payloadData);
        attachment.setEndUserSystemReference(endUserSystemReference);
        attachment.setEncrypted(false);
        attachment.setName("Amelding");
        attachment.setFileName("AmeldingPayload.zip");
        attachment.setParentReference(null);
        List<Attachment> attachments = new LinkedList<>();
        attachments.add(attachment);
        ReceiptExternalBE receiptExternalBE = inboundExternalEC2Client.sendFormTaskShipment(reportee, externalShipmentReference, serviceCode,
                serviceEdition, dataFormatId, dataFormatVersion, endUserSystemReference, formData, attachments);
        assertThat(receiptExternalBE.getReceiptStatusCode().value(), is("OK"));

        int receiptId = receiptExternalBE.getReceiptId();

        Integer receiverReference = null;
        while(receiverReference == null ) {
            Receipt receipt = receiptEC2Client.getReceipt(receiptId, null);
            assertThat(receipt, notNullValue());
            log.info("Receipt received: " + receipt.getReceiptId() + receipt.getReceiptText().getValue());
            receiverReference = findReceiverReference(receipt);
            Thread.sleep(1000);
        }

        CorrespondenceForEndUserSystemV2 correspondence = correspondenceEC2Client.getCorrespondenceForEndUserSystemsEC(receiverReference, LANGUAGEID_NB);
        log.info(correspondence.toString());
        List<AttachmentBEV2> returnAttachmentsFromTax = correspondence.getCorrespondenceAttachments().getValue().getAttachmentBEV2();
        for (AttachmentBEV2 a : returnAttachmentsFromTax) {
            byte[] attachmentdata = a.getAttachmentData().getValue();
            String attachmentName = "AMeldingResponse"+System.currentTimeMillis()+".zip";
            FileUtils.writeByteArrayToFile(new File(OUTPUT_DIR_AMELDING+attachmentName), attachmentdata);
        }
    }

    public Integer findReceiverReference(Receipt receipt){
        List<Reference2> refList = receipt.getReferences().getValue().getReference();
        for (Reference2 reference : refList) {
            if (reference.getReferenceType() == ReferenceType.RECEIVERS_REFERENCE) {
                return Integer.parseInt(reference.getReferenceValue());
            }
        }
        return null;
    }


}
