package org.dew.cms.backend.util;

import java.sql.*;
import javax.transaction.*;

public
class SimpleUserTransaction implements UserTransaction
{
  Connection conn;
  
  public
  SimpleUserTransaction(Connection theConnection)
  {
    this.conn = theConnection;
  }
  
  public
  void begin()
      throws javax.transaction.NotSupportedException, javax.transaction.SystemException
  {
    try {
      if(conn.getAutoCommit()) {
        conn.setAutoCommit(false);
      }
      conn.commit();
    }
    catch (SQLException ex) {
      throw new SystemException(ex.toString());
    }
  }
  
  public
  void commit()
      throws javax.transaction.RollbackException, javax.transaction.HeuristicMixedException, javax.transaction.HeuristicRollbackException, java.lang.SecurityException, java.lang.IllegalStateException, javax.transaction.SystemException
  {
    try {
      conn.commit();
    }
    catch (SQLException ex) {
      throw new SystemException(ex.toString());
    }
  }
  
  public
  void rollback()
      throws java.lang.IllegalStateException, java.lang.SecurityException, javax.transaction.SystemException
  {
    try {
      conn.rollback();
    }
    catch (SQLException ex) {
      throw new SystemException(ex.toString());
    }
  }
  
  public
  void setRollbackOnly()
      throws java.lang.IllegalStateException, javax.transaction.SystemException
  {
  }
  
  public
  int getStatus()
      throws javax.transaction.SystemException
  {
    return 0;
  }
  
  public
  void setTransactionTimeout(int iTimeOut)
      throws javax.transaction.SystemException
  {
  }
}
