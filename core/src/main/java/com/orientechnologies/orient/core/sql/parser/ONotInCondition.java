/* Generated By:JJTree: Do not edit this line. ONotInCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.orientechnologies.orient.core.sql.parser;

import java.util.Collection;

import com.orientechnologies.orient.core.db.record.OIdentifiable;

public class ONotInCondition extends OBooleanExpression {

  protected OExpression            left;
  protected OBinaryCompareOperator operator;
  protected OSelectStatement       rightStatement;
  protected Collection<Object>     rightCollection;
  protected Object                 right;
  protected Object                 rightParam;

  public ONotInCondition(int id) {
    super(id);
  }

  public ONotInCondition(OrientSql p, int id) {
    super(p, id);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean evaluate(OIdentifiable currentRecord) {
    return false;
  }

  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(left.toString());
    result.append(" NOT IN ");
    if (rightStatement != null) {
      result.append("(");
      result.append(rightStatement.toString());
      result.append(")");
    } else if (rightCollection != null) {
      result.append("[");
      boolean first = true;
      for (Object o : rightCollection) {
        if (!first) {
          result.append(", ");
        }
        result.append(convertToString(o));
        first = false;
      }

      result.append("]");
    } else if (right != null) {
      result.append(convertToString(right));
    } else if (rightParam != null) {
      result.append(convertToString(rightParam));
    }
    return result.toString();
  }

  private String convertToString(Object o) {
    if (o instanceof String) {
      return "\"" + ((String) o).replaceAll("\"", "\\\"") + "\"";
    }
    return o.toString();
  }
}
/* JavaCC - OriginalChecksum=8fb82bf72cc7d9cbdf2f9e2323ca8ee1 (do not edit this line) */