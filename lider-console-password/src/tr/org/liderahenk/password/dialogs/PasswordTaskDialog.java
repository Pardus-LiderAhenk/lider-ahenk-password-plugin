package tr.org.liderahenk.password.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.LiderConfirmBox;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.password.constants.PasswordConstants;
import tr.org.liderahenk.password.i18n.Messages;

/**
 * Task execution dialog for password plugin.
 * 
 * @author <a href="mailto:cemre.alpsoy@agem.com.tr">Cemre ALPSOY</a>
 * 
 */
public class PasswordTaskDialog extends DefaultTaskDialog {

	private static final Logger logger = LoggerFactory.getLogger(PasswordTaskDialog.class);
	private Label lblPassword;
	private Text txtPassword;
	private Button btnExecuteNow;

	public PasswordTaskDialog(Shell parentShell, Set<String> dnSet, boolean hideActivationDate) {
		super(parentShell, dnSet, hideActivationDate);
	}

	@Override
	public String createTitle() {
		return Messages.getString("CHANGE_LDAP_PASSWORD");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = SWT.DEFAULT;
		gd.heightHint = SWT.DEFAULT;
		composite.setLayoutData(gd);

		lblPassword = new Label(composite, SWT.NONE);
		lblPassword.setText(Messages.getString("PASSWORD"));

		txtPassword = new Text(composite, SWT.BORDER);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		return null;
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
		if (txtPassword == null || txtPassword.getText() == null || txtPassword.getText().isEmpty()) {
			throw new ValidationException(Messages.getString("ENTER_NEW_PASSWORD"));
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (txtPassword != null && txtPassword.getText() != null && !txtPassword.getText().isEmpty())
			map.put(PasswordConstants.PASSWORD, txtPassword.getText().toString());
		return map;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Execute task now
		btnExecuteNow = createButton(parent, 5000, Messages.getString("CHANGE_NOW"), false);
		btnExecuteNow.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/task-play.png"));
		btnExecuteNow.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Validation of task data
				if (validateTaskData()) {
					if (LiderConfirmBox.open(Display.getDefault().getActiveShell(),
							Messages.getString("TASK_EXEC_TITLE"), Messages.getString("TASK_EXEC_MESSAGE"))) {
						try {
							getProgressBar().setVisible(true);
							TaskRequest task = new TaskRequest(new ArrayList<String>(getDnSet()), DNType.USER,
									getPluginName(), getPluginVersion(), getCommandId(), getParameterMap(), null, null,
									new Date());
							IResponse response = TaskRestUtils.execute(task);
							if (response != null && response.getStatus() == RestResponseStatus.OK) {
								Notifier.success(null, Messages.getString("TASK_EXECUTED"));
							} else if (response != null && response.getStatus() == RestResponseStatus.ERROR) {
								if (response.getMessages() != null && !response.getMessages().isEmpty()) {
									Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"),
											StringUtils.join(response.getMessages(), ""));
								} else {
									Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
								}
							}
							getProgressBar().setVisible(false);
						} catch (Exception e1) {
							getProgressBar().setVisible(false);
							logger.error(e1.getMessage(), e1);
							Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
						}
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Close
		Button closeButton = createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("CANCEL"), true);
		closeButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				unsubscribeEventHandlers();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	@Override
	public String getCommandId() {
		return "CHANGE_LDAP_PASSWORD";
	}

	@Override
	public String getPluginName() {
		return PasswordConstants.PLUGIN_NAME;
	}

	@Override
	public String getPluginVersion() {
		return PasswordConstants.PLUGIN_VERSION;
	}

}
