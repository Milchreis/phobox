package de.milchreis.phobox.core.events;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.milchreis.phobox.core.Phobox;
import de.milchreis.phobox.core.events.model.EventLoopInfo;
import de.milchreis.phobox.core.model.PhoboxModel;
import de.milchreis.phobox.db.entities.Item;
import de.milchreis.phobox.helper.SpringDataProviderRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * All non-canon photos are from RAWSAMPLES.CH. Thank you very much Jakob Rohrbach for the nice project.
 */
@SpringBootTest
@RunWith(SpringDataProviderRunner.class)
public class MetaExtractEventTest {

    @Autowired
    private MetaExtractEvent metaExtractEvent;

    @BeforeClass
    public static void beforeAll() {
        PhoboxModel model = Phobox.getModel();
        model.setAutoSave(false);
        ReflectionTestUtils.setField(model, "storagePath", null);
    }

    @DataProvider
    public static Object[][] getWorkingImages() {
        return new Object[][]{
                // Filename         rotation  width   height  Camera          lens                               focallength  creation
                {"Canon-5D_01.CR2",     8,    4368,   2912,   "Canon EOS 5D", "Canon EF 28-90mm f/4-5.6 USM",    "90 mm",    "2019-07-24 07:14:06.0"},
                {"Canon-6D_01.CR2",     1,    4104,   2736,   "Canon EOS 6D", "EF85mm f/1.8 USM",                "85 mm",    "2019-06-10 13:43:46.0"},
                {"Canon-6D_01.jpg",     1,    4104,   2736,   "Canon EOS 6D", "EF85mm f/1.8 USM",                "85 mm",    "2019-06-10 13:43:46.0"},
                {"NIKON-D300S_01.NEF",  1,    4352,   2868,   "NIKON D300S",  "25-70mm f/3,2-5,6",               "25 mm",    "2009-12-16 21:07:45.0"},
                {"NIKON-D5000_01.NEF",  1,    4352,   2868,   "NIKON D5000",  "18-55mm f/3,5-5,6",               "18 mm",    "2009-05-23 13:32:40.0"},
                {"SONY-A6000_01.ARW",   1,    6000,   4000,   "SONY ILCE-6000",  null,                           null,       "2015-04-05 10:20:47.0"},
                {"FUJI-X100_01.RAF",    1,    2176,   1448,   "FUJIFILM FinePix X100",  null,                    "23 mm",    "2011-05-23 11:51:28.0"},
        };
    }

    @Test
    @UseDataProvider("getWorkingImages")
    public void test_getMetaData(String filename, Integer rotation, Integer width, Integer height, String camera, String lens, String focallength, String creation) {

        // Arrange
        File image = new File("src/test/resources/example-images/", filename);
        EventLoopInfo eventLoopInfo = new EventLoopInfo();
        Item item = new Item();
        eventLoopInfo.setItem(item);
        item.setFullPath(image.getAbsolutePath());

        // Act
        metaExtractEvent.onImportFile(image, eventLoopInfo);

        // Assert
        item = eventLoopInfo.getItem();
        assertNotNull(item);

        assertEquals(lens,          item.getLens());
        assertEquals(focallength,   item.getFocalLength());
        assertEquals(rotation,      item.getRotation());
        assertEquals(height,        item.getHeight());
        assertEquals(width,         item.getWidth());
        assertEquals(creation,      item.getCreation().toString());
        assertEquals(camera,        item.getCamera());
    }

}