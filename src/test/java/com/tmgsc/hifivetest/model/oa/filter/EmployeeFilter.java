// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.filter;

import com.tmgsc.hifivetest.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import java.util.*;

@OAClass(addToCache=false, initialize=true, useDataSource=false, localOnly=true)
public class EmployeeFilter extends OAObject {
    private static final long serialVersionUID = 1L;


    public HubFilter createNextAnniverseriesFilter(Hub<Employee> hubMaster, Hub<Employee> hub) {
        return createNextAnniverseriesFilter(hubMaster, hub, false);
    }
    public HubFilter createNextAnniverseriesFilter(final Hub<Employee> hubMaster, Hub<Employee> hub, boolean bAllHubs) {
        HubFilter filter = new HubFilter(hubMaster, hub) {
            @Override
            public boolean isUsed(Object object) {
                Employee employee = (Employee) object;
                return isUsedForNextAnniverseriesFilter(employee);
            }
        };
        filter.addDependentProperty(Employee.PROPERTY_HireDate);
        filter.addDependentProperty(OAString.cpp(Employee.PROPERTY_Program, Program.PROPERTY_AnniversaryDisplayDays));
 
        if (!bAllHubs) return filter;
        filter.setServerSideOnly(true); 
        // need to listen to all Employee
        HubCacheAdder hubCacheAdder = new HubCacheAdder(hubMaster);
        return filter;
    }

    public boolean isUsedForNextAnniverseriesFilter(Employee employee) {
        OADate hireDate = employee.getHireDate();
        if (hireDate == null) return false;
    
        int amt = 0;
        Program program = employee.getProgram();
        if (program != null) {
            amt = program.getAnniversaryDisplayDays();
        }
        if (amt < 1) amt = 30;
        
        OADate d = employee.getNextAnniversaryDate();
        if (d == null) return false;
        OADate today = new OADate();
        return (today.before(d) && d.before(today.addDays(amt)));
    }
    public HubFilter createNextBirthdaysFilter(Hub<Employee> hubMaster, Hub<Employee> hub) {
        return createNextBirthdaysFilter(hubMaster, hub, false);
    }
    public HubFilter createNextBirthdaysFilter(final Hub<Employee> hubMaster, Hub<Employee> hub, boolean bAllHubs) {
        HubFilter filter = new HubFilter(hubMaster, hub) {
            @Override
            public boolean isUsed(Object object) {
                Employee employee = (Employee) object;
                return isUsedForNextBirthdaysFilter(employee);
            }
        };
        filter.addDependentProperty(Employee.PROPERTY_BirthDate);
        filter.addDependentProperty(Employee.PROPERTY_Program);
 
        if (!bAllHubs) return filter;
        filter.setServerSideOnly(true); 
        // need to listen to all Employee
        HubCacheAdder hubCacheAdder = new HubCacheAdder(hubMaster);
        return filter;
    }

    public boolean isUsedForNextBirthdaysFilter(Employee employee) {
        OADate birthDate = employee.getBirthDate();
        if (birthDate == null) return false;
    
        int amt = 0;
        Program program = employee.getProgram();
        if (program != null) amt = program.getBirthdayDisplayDays();
        if (amt < 1) amt = 30;
        
        OADate today = new OADate();
    
        OADate dateNext = new OADate(birthDate);
        dateNext.setYear(today.getYear());
        
        int diff = dateNext.compareTo(today);
        if (diff < 0) {
            dateNext.setYear(today.getYear()+1);
            diff = dateNext.compareTo(today);
        }
        
        return diff >= 0 && diff <= amt;
    }
}
