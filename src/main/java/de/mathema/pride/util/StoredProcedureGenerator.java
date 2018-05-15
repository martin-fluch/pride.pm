/*******************************************************************************
 * Copyright (c) 2001-2007 The PriDE team and MATHEMA Software GmbH
 * All rights reserved. This toolkit and the accompanying materials 
 * are made available under the terms of the GNU Lesser General Public
 * License (LGPL) which accompanies this distribution, and is available
 * at http://pride.sourceforge.net/LGPL.html
 * 
 * Contributors:
 *     Jan Lessner, MATHEMA Software GmbH - initial API and implementation
 *******************************************************************************/
package de.mathema.pride.util;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import de.mathema.pride.*;
import de.mathema.util.ArgReader;
import de.mathema.util.ArgumentException;

/**
 * Generator for stored procedure access classes. This generator
 * works for Oracle databases only, due to the vendor-specific
 * structure of SP meta data.
 *
 * @author <a href="mailto:jan.lessner@mathema.de">Jan Lessner</a>
 */
public class StoredProcedureGenerator
{
    protected static final String IN = "IN";
    protected static final String OUT = "OUT";
    protected static final String INOUT = "INOUT";

    protected String pack, proc, cls;

    protected String[] memberExcludeList() { return null; }
    
    public void generatePackageAndImports(PrintStream out) {
        out.println("package ???.????.????;\n");
        out.println("import java.sql.Date;");
    }

    public void generateClassHeader(PrintStream out) {
        out.println("/**\n * Generated by Stored Procedure Generator, " + new java.util.Date());
        out.println(" * @author <a href=\"mailto:jan.lessner@mathema.de\">Jan Lessner</a>\n */");
        out.println("class " + cls + " extends StoredProcedure\n{");
    }

    public String sql2javaType(String sqltype, Long length) throws Exception {
        if (sqltype.equals("NUMBER")) return (length != null && length.intValue() < 5) ? "int" : "long";
        if (sqltype.equals("VARCHAR")) return "String";
        if (sqltype.equals("VARCHAR2")) return "String";
        if (sqltype.equals("DATE")) return "Date";
        return null;
    }
    
    public String sql2javaMember(String name) {
        StringBuffer javaMember = new StringBuffer();
        boolean nextUpper = false;
        
        name = name.toLowerCase();
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '_')
                nextUpper = true;
            else {
                if (nextUpper) {
                    javaMember.append(Character.toUpperCase(name.charAt(i)));
                    nextUpper = false;
                }
                else
                    javaMember.append(name.charAt(i));
            }
        }
        return javaMember.toString();
    }

    public void generateMember(PrintStream out, String name, String io, String type, Long length) throws Exception {
        name = name.toLowerCase();
        String[] excludeList = memberExcludeList();
        if (excludeList != null) {
            for (int i = 0; i < excludeList.length; i++) {
                if (name.equals(excludeList[i]))
                    return;
            }
        }
        out.print("    public ");
        if (io.equals(IN))
            out.print("final ");
        out.print(sql2javaType(type, length) + " " + sql2javaMember(name) + ";\n");
    }
    
    public void generateMembers(PrintStream out) throws Exception {
        AllArguments args = new AllArguments();
        args.setObjectName(proc);
        args.setPackageName(pack);
        String[] queryFields = (pack == null) ?
            new String[] { "object_name" } :
            new String[] { "object_name", "package_name" };
        
        String constraint = args.constraint( queryFields );
        ResultIterator iter = args.query(constraint + " order by position");
        pack = args.getPackageName();
        do {
            generateMember(out, args.getArgumentName(), args.getInOut(), args.getDataType(), args.getDataLength());
        } while (iter.next());
    }

    public void generateName(PrintStream out) {
        out.println("    protected String getName() { return \"" +
                    ((pack == null) ? "" : (pack + ".")) + proc + "\"; }\n");
    }

    public void generateCtor(PrintStream out) {
        out.println("    public " + cls + "() { }\n");
    }

    public void generateClassTrailer(PrintStream out) {
        out.println("    public final static String REVISION_ID = \"$Header:   //DEZIRWD6/PVCSArchives/dmd3000-components/framework/pride/src/de/mathema/pride/util/StoredProcedureGenerator.java-arc   1.1   11 Sep 2002 13:25:08   math19  $\";");
        out.println("}");
    }
    
    public void generate() throws Exception {
        PrintStream out = new PrintStream(new FileOutputStream(cls + ".java"));
        generatePackageAndImports(out);
        generateClassHeader(out);
        generateMembers(out);
        generateName(out);
        generateCtor(out);
        generateClassTrailer(out);
        out.close();
    }
    
    public static void error(String message) {
        System.out.println(message);
        System.exit(1);
    }

    public static void help() {
    }

    public StoredProcedureGenerator(String[] args) throws Exception {
        ArgReader argReader = new ArgReader(args, "p:s:c:d:u:w:i:");
        Properties props = new Properties();
        String db = null;
        char option = ArgReader.ARGEND;
        
        do {
            try {
                switch(option = argReader.getArg()) {
                case 'p':
                    pack = argReader.getArgValue();
                    break;
                case 's':
                    proc = argReader.getArgValue();
                    break;
                case 'c':
                    cls = argReader.getArgValue();
                    break;
                case 'u':
                	props.setProperty(ResourceAccessor.Config.USER, argReader.getArgValue());
                    break;
                case 'd':
					props.setProperty(ResourceAccessor.Config.DRIVER, argReader.getArgValue());
                    break;
                case 'w':
					props.setProperty(ResourceAccessor.Config.PASSWORD, argReader.getArgValue());
                    break;
                case 'i':
                    db = argReader.getArgValue();
                    break;
                }
            }
            catch(ArgumentException ax) {
                System.out.println(ax.getMessage());
                help();
            }
        } while (option != ArgReader.ARGEND);

        if (proc == null)
            error("Missing stored procedure name");
        if (db == null)
            error("Missing database name");
        if (props.getProperty(ResourceAccessor.Config.DRIVER) == null)
            error("Missing driver name");
        if (cls == null)
            cls = proc;
        props.setProperty(ResourceAccessor.Config.LOGFILE, "sql.log");
        DatabaseFactory.setDatabaseName(db);
        DatabaseFactory.setResourceAccessor(new ResourceAccessorJ2SE(props));
    }
    
    public static void main(String[] args) throws Exception {
        new StoredProcedureGenerator(args).generate();
    }

    public final static String REVISION_ID = "$Header:   //DEZIRWD6/PVCSArchives/dmd3000-components/framework/pride/src/de/mathema/pride/util/StoredProcedureGenerator.java-arc   1.1   11 Sep 2002 13:25:08   math19  $";
}

/* $Log:   //DEZIRWD6/PVCSArchives/dmd3000-components/framework/pride/src/de/mathema/pride/util/StoredProcedureGenerator.java-arc  $
 * 
 *    Rev 1.1   11 Sep 2002 13:25:08   math19
 * Smoothed and inline documented.
 * 
 *    Rev 1.0   06 Sep 2002 14:51:30   math19
 * Initial revision.
 * 
 *    Rev 1.2   Jun 17 2002 13:02:44   math19
 * Doesn't generate base class members of WebmilesProcedure any more.
 */