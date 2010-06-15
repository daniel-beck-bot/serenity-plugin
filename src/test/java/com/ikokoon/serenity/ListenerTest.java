package com.ikokoon.serenity;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

public class ListenerTest extends ATest {

	private static IDataBase dataBase;

	@BeforeClass
	public static void beforeClass() {
		ATest.beforeClass();
		String dataBaseFile = "./src/test/resources/tag.odb";
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, mockInternalDataBase);
	}

	@Test
	public void listen() throws Exception {
		Listener.listen(dataBase);
		Messenger.main(new String[] { "127.0.0.1", "report" });
		Thread.sleep(10000);
		// Verify that the reports are written
		File file = new File("./serenity");
		logger.warn("Searching directory : " + file.getAbsolutePath());
		List<File> files = new ArrayList<File>();
		Toolkit.findFiles(file, new Toolkit.IFileFilter() {
			public boolean matches(File file) {
				return file.getName().equals(Reporter.METHOD_SERIES);
			}
		}, files);
		logger.warn("Report files : " + files);
		assertTrue(files.size() > 0);
	}

}