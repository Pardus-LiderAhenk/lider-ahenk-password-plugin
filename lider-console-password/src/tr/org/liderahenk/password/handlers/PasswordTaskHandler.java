package tr.org.liderahenk.password.handlers;

import java.util.Set;

import org.eclipse.swt.widgets.Display;

import tr.org.liderahenk.liderconsole.core.handlers.MultipleSelectionHandler;
import tr.org.liderahenk.password.dialogs.PasswordTaskDialog;

public class PasswordTaskHandler extends MultipleSelectionHandler {

	@Override
	public void executeWithDNSet(Set<String> dnSet) {
		PasswordTaskDialog dialog = new PasswordTaskDialog(Display.getDefault().getActiveShell(), dnSet, true);
		dialog.create();
		dialog.open();
	}
}
