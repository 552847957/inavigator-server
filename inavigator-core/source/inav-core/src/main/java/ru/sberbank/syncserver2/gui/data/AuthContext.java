package ru.sberbank.syncserver2.gui.data;

/*
 * Copyright: Copyright (c) 2005
 * @author    Leonid Kozhinsky
 * @module    $
 * @version   $
 * @last mod  $
 */

import java.io.Serializable;

public class AuthContext implements Cloneable, Serializable {

	public static final int GENERAL_FAILURE  = 1;
	public static final int WRONG_USERNAME   = 2;
	public static final int WRONG_PASSWORD   = 3;
	public static final int NO_PERMISSION    = 4;
	public static final int VALID_USER       = 5;

    private int status;
	private Employee employee;
    private long lastUsed;

    /**
	 * @param status
	 */
	public AuthContext(int status) {
		this.status = status;
        this.lastUsed = System.currentTimeMillis();
    }

	public AuthContext(Employee employee) {
		this.employee = employee;
		this.status = VALID_USER;
        this.lastUsed = System.currentTimeMillis();
	}

	public Employee getEmployee() {
		return employee;
	}

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    /**
	 * @return Returns the status.
	 */
	public int getStatus() {
		return status;
	}

    public synchronized long getLastUsed() {
        return lastUsed;
    }

    public synchronized void setLastUsed(){
        this.lastUsed = System.currentTimeMillis();
    }

    public String toString() {
		return status == VALID_USER ? employee.getEmployeeEmail() : "Unknown";
	}

	/**
	 * @return true if user a site admin
	 */
	public static boolean isValidUser(Employee employee) {
        return employee!=null;
	}

	public void setStatus(int status) {
		this.status = status;
	}

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

}
