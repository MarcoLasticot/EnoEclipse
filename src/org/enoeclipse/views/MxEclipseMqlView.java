package org.enoeclipse.views;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.enoeclipse.Activator;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class MxEclipseMqlView extends ViewPart
{
  public static final String VIEW_ID = MxEclipseMqlView.class.getName();

  private Composite top = null;

  private Text txtOutput = null;

  private Label label = null;

  private Label lblInputBox = null;

  private Text txtInput = null;

  private Button cmdEnter = null;

  private int count = 0;
  private int current = 1;

  private ArrayList<String> history = new ArrayList<String>();

  private Button cmdClear = null;
  private Image imgClear;

  public MxEclipseMqlView()
    throws Exception
  {
  }

  public void createPartControl(Composite parent)
  {
    GridData gridData11 = new GridData();
    gridData11.horizontalAlignment = 3;
    gridData11.verticalAlignment = 2;
    GridData gridData4 = new GridData();
    gridData4.horizontalAlignment = 4;
    gridData4.verticalAlignment = 2;
    GridData gridData3 = new GridData();
    gridData3.horizontalSpan = 2;
    GridData gridData2 = new GridData();
    GridData gridData1 = new GridData();
    gridData1.horizontalAlignment = 4;
    gridData1.grabExcessHorizontalSpace = true;
    gridData1.verticalAlignment = 4;
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    GridData gridData = new GridData();
    gridData.horizontalAlignment = 4;
    gridData.grabExcessVerticalSpace = true;
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalSpan = 2;
    gridData.verticalAlignment = 4;
    this.top = new Composite(parent, 0);
    this.top.setLayout(gridLayout);
    this.label = new Label(this.top, 0);
    this.label.setText("Output Pane");
    this.label.setLayoutData(gridData2);
    this.cmdClear = new Button(this.top, 8388608);
    this.imgClear = Activator.getImageDescriptor("eraser.gif").createImage();
    this.cmdClear.setImage(this.imgClear);
    this.cmdClear.setToolTipText("Clear");
    this.cmdClear.setLayoutData(gridData11);
    this.cmdClear.setText("");
    this.cmdClear.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        MxEclipseMqlView.this.txtOutput.setText("");
      }
    });
    this.txtOutput = new Text(this.top, 586);
    this.txtOutput.setEditable(true);
    this.txtOutput.setEnabled(true);
    this.txtOutput.setLayoutData(gridData);
    this.lblInputBox = new Label(this.top, 0);
    this.lblInputBox.setText("Input MQL");
    this.lblInputBox.setLayoutData(gridData3);
    this.txtInput = new Text(this.top, 2048);
    this.txtInput.setLayoutData(gridData1);

    this.txtInput.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == 13)
			try {
				MxEclipseMqlView.this.enterPressed();
			} catch (MatrixException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		else if (e.keyCode == 16777217)
        {
          MxEclipseMqlView.this.previousEntry();
        } else if (e.keyCode == 16777218)
        {
          MxEclipseMqlView.this.nextEntry();
        }
      }
    });
    this.cmdEnter = new Button(this.top, 0);
    this.cmdEnter.setText("Enter");
    this.cmdEnter.setLayoutData(gridData4);
    this.cmdEnter.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        try {
			MxEclipseMqlView.this.enterPressed();
		} catch (MatrixException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      }
    });
  }

  public void setFocus()
  {
  }

  private void enterPressed() throws MatrixException
  {
    this.count += 1; this.current = this.count;
    String inputText = this.txtInput.getText();
    this.txtOutput.append("MQL " + this.count + "> " + inputText + "\n");
    this.history.add(this.txtInput.getText());
    this.txtInput.setText("");

    Context context = Activator.getDefault().getContext();
    if ((context != null) && (context.isConnected()))
    {
      MQLCommand command = new MQLCommand();
      try {
        command.executeCommand(context, inputText);
        String res = command.getResult();
        res = res + command.getError();
        this.txtOutput.append(res);
      } catch (MatrixException e) {
        e.printStackTrace();
      }
    } else {
      MessageDialog.openInformation(this.top.getShell(), 
        "Search Matrix Admin Objects", 
        "No User connected to Matrix");
    }
  }

  private void previousEntry() {
    if (this.current > 0) {
      this.current -= 1;
      this.txtInput.setText((String)this.history.get(this.current));
      this.txtInput.setSelection(((String)this.history.get(this.current)).length());
    }
  }

  private void nextEntry() {
    if (this.current < this.count - 1) {
      this.current += 1;
      this.txtInput.setText((String)this.history.get(this.current));
      this.txtInput.setSelection(((String)this.history.get(this.current)).length());
    }
  }

  public void dispose()
  {
    this.imgClear.dispose();
    super.dispose();
  }
}