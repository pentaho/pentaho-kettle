package org.eobjects.datacleaner.kettle.jobentry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class EnumCombo<E extends Enum<?>> extends Composite {

	private final Combo _combo;
	private final Class<E> _enumClass;
	private final boolean _nullable;

	public EnumCombo(Composite composite, Class<E> enumClass, boolean nullable) {
		super(composite, SWT.None);
		_nullable = nullable;
		_enumClass = enumClass;
		_combo = WidgetFactory.createCombo(this);
		_combo.setLayoutData(null);
		setLayout(new FillLayout());
		setLayoutData(WidgetFactory.createGridData());

		E[] constants = _enumClass.getEnumConstants();
		if (nullable) {
			_combo.add("");
		}
		for (E item : constants) {
			String stringValue = getStringValue(item);
			_combo.add(stringValue);
		}
	}

	private String getStringValue(E item) {
		return item.toString();
	}

	public void addModifyListener(ModifyListener modifyListener) {
		_combo.addModifyListener(modifyListener);
	}

	public void removeModifyListener(ModifyListener modifyListener) {
		_combo.removeModifyListener(modifyListener);
	}

	public void setValue(E value) {
		if (value == null) {
			if (_nullable) {
				_combo.setText("");
			} else {
				throw new IllegalArgumentException("EnumCombo is not nullable!");
			}
		} else {
			_combo.setText(getStringValue(value));
		}
	}

	public E getValue() {
		final String text = _combo.getText();
		E[] constants = _enumClass.getEnumConstants();
		for (E item : constants) {
			if (text.equals(getStringValue(item))) {
				return item;
			}
		}
		if (_nullable) {
			return null;
		} else {
			throw new IllegalStateException(
					"No value found that matches string: " + text);
		}
	}
}
