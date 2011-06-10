package au.org.ala.delta.editor.directives;

import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;

import au.org.ala.delta.DeltaContext;
import au.org.ala.delta.directives.AbstractDeltaContext;
import au.org.ala.delta.directives.AbstractDirective;
import au.org.ala.delta.directives.ConforDirectiveFileParser;
import au.org.ala.delta.directives.DirectiveParserObserver;
import au.org.ala.delta.editor.DeltaEditor;
import au.org.ala.delta.editor.directives.ui.ImportExportDialog;
import au.org.ala.delta.editor.directives.ui.ImportExportDialog.DirectiveFile;
import au.org.ala.delta.editor.directives.ui.ImportExportStatusDialog;
import au.org.ala.delta.editor.model.EditorDataModel;

public class ExportController {
	private DeltaEditor _context;
	private EditorDataModel _model;
	
	public ExportController(DeltaEditor context) {
		_context = context;
		_model = context.getCurrentDataSet();
	}
	
	public void begin() {
		
		if ((_model.getNumberOfCharacters() > 0) || (_model.getMaximumNumberOfItems() > 0)) {
			JOptionPane.showMessageDialog(_context.getMainFrame(), "Imports are only currently supported for new data sets.");
			return;
		}
		ImportExportDialog dialog = new ImportExportDialog(_context.getMainFrame());
		_context.show(dialog);
		
		if (dialog.proceed()) {
			List<DirectiveFile> files = dialog.getSelectedFiles();
			File selectedDirectory = dialog.getSelectedDirectory();
			
			doImport(selectedDirectory, files);
		}
	}
	
	public void doImport(File selectedDirectory, List<DirectiveFile> files) {
		ImportExportStatusDialog statusDialog = new ImportExportStatusDialog(_context.getMainFrame());
		_context.show(statusDialog);
		
		// Do the import on a background thread.
		DoExportTask importTask = new DoExportTask(selectedDirectory, files);
		importTask.addTaskListener(new StatusUpdater(statusDialog));
		importTask.execute();
	}
	
	
	public class DoExportTask extends Task<Void, ImportExportStatus> implements DirectiveParserObserver {

		private String _directoryName;
		private List<DirectiveFile> _files;
		
		public DoExportTask(File directory, List<DirectiveFile> files) {
			super(_context);
			String directoryName = directory.getAbsolutePath();
			if (!directoryName.endsWith(File.separator)) {
				directoryName += File.separator;
			}
			_directoryName = directoryName;
			_files = files;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			ImportExportStatus status = new ImportExportStatus();
			publish(status);
						
			DeltaContext context = new DeltaContext(_model);
			ConforDirectiveFileParser parser = ConforDirectiveFileParser.createInstance();
			int fileNumber = 1;
			for (DirectiveFile file : _files) {
				
				
				// First check if the existing dataset has a directives file with the same name
				// and same last modified date.  If so, skip it.
				status.setCurrentFile(file._fileName);
				publish(status);
				// Looks like we skip the specs file if we have non zero items or chars.....
				try {
				
					File directiveFile = new File(_directoryName+file._fileName);
					parser.parse(directiveFile, context);
				}
				catch (Exception e) {
					status.setTotalErrors(status.getTotalErrors()+1);
					e.printStackTrace();
				}
				status.setTotalLines(status.getTotalLines()+1);
				publish(status);
			}
			
			return null;
		}

		@Override
		protected void failed(Throwable cause) {
			cause.printStackTrace();
			super.failed(cause);
		}	
			
		@Override
		public void preProcess(String data) {}

		@Override
		public void postProcess(AbstractDirective<? extends AbstractDeltaContext> directive) {
			//storeDirective(directive);
		}
	}
	
	/**
	 * Listens for import progress and updates the Status Dialog.
	 */
	private class StatusUpdater extends TaskListener.Adapter<Void, ImportExportStatus> {

		private ImportExportStatusDialog _statusDialog;
		
		public StatusUpdater(ImportExportStatusDialog statusDialog) {
			_statusDialog = statusDialog;
		}
		@Override
		public void process(TaskEvent<List<ImportExportStatus>> event) {
			
			_statusDialog.update(event.getValue().get(0));
		}
		
	}
}