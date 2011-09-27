package reverse.engineer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import reverse.engineer.Settings.Changeable;

public class SettingsGui extends JFrame implements ActionListener {

    public static final Dimension size = new Dimension(800, 400);

    JPanel allPanel = new JPanel();
    
    JButton save, cancel;
    
    public SettingsGui() {
        this.setSize(size);
        this.setPreferredSize(size);

        final ArrayList<Field> settingFields = new ArrayList<Field>();

        for (final Field f : Settings.class.getFields()) {
            if (f.isAnnotationPresent(Changeable.class)) {
                settingFields.add(f);
            }
        }

        allPanel.setLayout(new GridLayout(settingFields.size() + 1, 2));

        for (final Field f : settingFields) {
            allPanel.add(new JLabel(f.getAnnotation(Changeable.class).name()));
            final Class<?> clazz = f.getType();
            try {
                if (clazz == Boolean.class) {
                    final MyCheckBox check = new MyCheckBox();
                    check.myField = f;
                    allPanel.add(check);
                    check.setSelected(f.get(null) == Boolean.TRUE);
                } else if (clazz == String.class) {
                    final MyTextField field = new MyTextField((String) f.get(null));
                    field.myField = f;
                    allPanel.add(field);
                } else if (Enum.class.isAssignableFrom(clazz)) {
                    
                    final DefaultComboBoxModel model = new DefaultComboBoxModel();
                    
                    try {
                        final Method valuesMethod = clazz.getMethod("values");
                        final Object o = valuesMethod.invoke(null);
                        final Object[] objs = (Object[]) o;
                        for (final Object oo : objs) {
                            model.addElement(oo);
                            System.out.println(oo);
                        }
                     } catch (final SecurityException e) {
                        e.printStackTrace();
                    } catch (final NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (final InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    
                    final MyComboBox combo = new MyComboBox(model);
                    combo.setEditable(false);
                    
                    combo.myField = f;
                    
                    allPanel.add(combo);
                }
                else {
                    System.out.println("Unidentified clazz " + clazz.getName());
                }
            } catch (final IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        save = new JButton("Save");
        cancel = new JButton("Cancel");
        
        save.addActionListener(this);
        cancel.addActionListener(this);
        
        allPanel.add(save);
        allPanel.add(cancel);
        
        this.add(allPanel);

        this.setVisible(true);
    }
    
    public static void main(final String[] args) {
        new SettingsGui();
    }
    
    private class MyTextField extends JTextField implements MyComponent {
        public Field myField;
        public MyTextField(final String s) {
            super(s);
        }
        @Override
        public Field getMyField() {
            return myField;
        }
    }
    
    private class MyCheckBox extends JCheckBox implements MyComponent {
        public Field myField;

        @Override
        public Field getMyField() {
            return myField;
        }
    }
    
    private class MyComboBox extends JComboBox implements MyComponent {
        public Field myField;
        public MyComboBox(final ComboBoxModel model) {
            super(model);
        }
        @Override
        public Field getMyField() {
            return myField;
        }
    }
    
    private interface MyComponent {
        public Field getMyField();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Object src = e.getSource();
        if (src == save) {
            
            boolean reqRestart = false;
            
            for (final Component c : allPanel.getComponents()) {
                if (!(c instanceof MyComponent)) {
                    continue;
                }
                final MyComponent myc = (MyComponent) c;
                
                Object oldVal = null;
                try {
                    oldVal = myc.getMyField().get(null);
                } catch (final IllegalArgumentException e2) {
                    e2.printStackTrace();
                } catch (final IllegalAccessException e2) {
                    e2.printStackTrace();
                }
                Object val = null;
                
                if (c instanceof MyCheckBox) {
                    val = ((MyCheckBox)c).isSelected();
                }
                else if (c instanceof MyTextField) {
                    val = ((MyTextField)c).getText();
                }
                else if (c instanceof MyComboBox) {
                    
                    // Must be enum?
                    /*
                    final Class<?> fClass = myc.getMyField().getType();
                    try {
                        final Method valueOfMethod = fClass.getMethod("valueOf", String.class);
                        final Object o = valueOfMethod.invoke(null, ((MyComboBox)c).getSelectedItem());
                        if (o != null) {
                            val = o;
                        }
                    } catch (final SecurityException e1) {
                        e1.printStackTrace();
                    } catch (final NoSuchMethodException e1) {
                        e1.printStackTrace();
                    } catch (final IllegalArgumentException e2) {
                        e2.printStackTrace();
                    } catch (final IllegalAccessException e2) {
                        e2.printStackTrace();
                    } catch (final InvocationTargetException e2) {
                        e2.printStackTrace();
                    }*/
                    val = ((MyComboBox)c).getSelectedItem();
                    
                }
                
                try {
                    myc.getMyField().set(null, val);
                    System.out.println(myc.getMyField().getName() + " set to " + val);
                } catch (final IllegalArgumentException e1) {
                    e1.printStackTrace();
                } catch (final IllegalAccessException e1) {
                    e1.printStackTrace();
                }
                
                if (val != oldVal && myc.getMyField().getAnnotation(Changeable.class).reqRestart()) {
                    reqRestart = true;
                }
                
            }
            
            try {
                Settings.storeProps();
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
            
            if (reqRestart) {
                JOptionPane.showMessageDialog(this, "One or more of your changes require restart to be applied", "Restart required", JOptionPane.WARNING_MESSAGE);
            }
            
            dispose();
        }
        else if (src == cancel) {
            dispose();
        }
    }
}
