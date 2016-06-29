package org.enoeclipse.model;

import org.enoeclipse.exception.EnoEclipseException;

import matrix.util.MatrixException;


public class EnoTreeRange extends EnoTreeBusiness
{
  public static final String CONDITION_EQUAL = "=";
  public static final String CONDITION_NOT_EQUAL = "!=";
  public static final String CONDITION_LESS_THAN = "<";
  public static final String CONDITION_LESS_OR_EQUAL = "<=";
  public static final String CONDITION_GREATER_THAN = ">";
  public static final String CONDITION_GREATER_OR_EQUAL = ">=";
  public static final String CONDITION_MATCH = "match";
  public static final String CONDITION_NOT_MATCH = "!match";
  public static final String CONDITION_STRING_MATCH = "smatch";
  public static final String CONDITION_NOT_STRING_MATCH = "!smatch";
  private boolean newAttribute;
  private String condition;

  public EnoTreeRange(String name)
    throws EnoEclipseException, MatrixException
  {
    super("Range", name);
  }

  public boolean isNewAttribute() {
    return this.newAttribute;
  }

  public String getCondition() {
    return this.condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public boolean equals(Object obj)
  {
    if ((obj instanceof EnoTreeRange)) {
      EnoTreeRange otherRange = (EnoTreeRange)obj;
      return this.name.equals(otherRange.name);
    }
    return false;
  }
}