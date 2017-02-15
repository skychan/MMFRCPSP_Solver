// ---------------------------------------------------------------*- Java -*-
// File: ./examples/src/java/SchedRCPSPMM.java
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5725-A06 5725-A29
// Copyright IBM Corporation 1990, 2014. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
// --------------------------------------------------------------------------

/* ------------------------------------------------------------

Problem Description
-------------------

The MMRCPSP (Multi-Mode Resource-Constrained Project Scheduling
Problem) is a generalization of the Resource-Constrained Project
Scheduling problem (see SchedRCPSP.java). In the MMRCPSP, each
activity can be performed in one out of several modes. Each mode of an
activity represents an alternative way of combining different levels
of resource requirements with a related duration. Renewable and
no-renewable resources are distinguished. While renewable resources
have a limited instantaneous availability such as manpower and
machines, non renewable resources are limited for the entire project,
allowing to model, e.g., a budget for the project.  The objective is
to find a mode and a start time for each activity such that the
schedule is makespan minimal and feasible with regard to the
precedence and resource constraints.

------------------------------------------------------------ */

import ilog.concert.*;
import ilog.cp.*;
import java.io.*;
import java.util.*;

public class SchedRCPSPMM {

//    private static class DataReader {
//
//        private StreamTokenizer st;
//
//        public DataReader(String filename) throws IOException {
//            FileInputStream fstream = new FileInputStream(filename);
//            Reader r = new BufferedReader(new InputStreamReader(fstream));
//            st = new StreamTokenizer(r);
//        }
//
//        public int next() throws IOException {
//            st.nextToken();
//            return (int) st.nval;
//        }
//    }

    static class IntervalVarList extends ArrayList<IloIntervalVar> {
        public IloIntervalVar[] toArray() {
            return (IloIntervalVar[]) this.toArray(new IloIntervalVar[this.size()]);
        }
    }

    static IloIntExpr[] arrayFromList(List<IloIntExpr> list) {
        return (IloIntExpr[]) list.toArray(new IloIntExpr[list.size()]);
    }

//    public static void main(String[] args) throws IOException {
    public static void solve(String projectFileName, String enterpriseFileName) throws IOException {
//        String filename = "data/m12_1.mm";
        int failLimit = 30000;
        int nbTasks, nbRenewable, nbNonRenewable;
        int duedate;
        
        int nbEnterprise;
        List<Enterprise> enterprises = new ArrayList<Enterprise>();
//        if (args.length > 0)
//            filename = args[0];
//        if (args.length > 1)
//            failLimit = Integer.parseInt(args[1]);

        IloCP cp = new IloCP();
        
        DataReader entData = new DataReader(enterpriseFileName);
        
        try {
			nbEnterprise = (int) entData.next();
			for (int i = 0; i < nbEnterprise; i++) {
				int idx = (int) entData.next();
				double x = entData.next();
				double y = entData.next();
				double quality = entData.next();
				Enterprise e = new Enterprise(x, y);
				e.setIndex(idx);
				e.setQuality(quality);
				
				int nbType = (int) entData.next();
				
				for (int j = 0; j < nbType; j++) {
					int type = (int) entData.next();
					int amount = (int) entData.next();
					int cost = (int) entData.next();
					e.setResourceAmount(type, amount);
					e.setResourceCost(type, cost);
				}
				
			}
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("Error: " + e);
		}
        
        DataReader data = new DataReader(projectFileName);
        
        try {        	
            nbTasks = (int) data.next();
            nbRenewable = (int) data.next();
            nbNonRenewable = (int) data.next();
            IloCumulFunctionExpr[] renewables = new IloCumulFunctionExpr[nbRenewable];
            IloIntExpr[] nonRenewables = new IloIntExpr[nbNonRenewable];
            int[] capRenewables = new int[nbRenewable];
            int[] capNonRenewables = new int[nbNonRenewable];
            for (int j = 0; j < nbRenewable; j++) {
                renewables[j] = cp.cumulFunctionExpr();
                capRenewables[j] = (int) data.next();
            }
            for (int j = 0; j < nbNonRenewable; j++) {
                nonRenewables[j] = cp.intExpr();
                capNonRenewables[j] = (int) data.next();
            }
            
            duedate = (int) data.next();
            
            IloIntervalVar[] tasks = new IloIntervalVar[nbTasks];
            IntervalVarList[] modes = new IntervalVarList[nbTasks];
            for (int i = 0; i < nbTasks; i++) {
                tasks[i] = cp.intervalVar();
                modes[i] = new IntervalVarList();
            }
            List<IloIntExpr> ends = new ArrayList<IloIntExpr>();
            for (int i = 0; i < nbTasks; i++) {
                IloIntervalVar task = tasks[i];
                int d = (int) data.next();
                int nbModes = (int) data.next();
                int nbSucc = (int) data.next();
                for (int k = 0; k < nbModes; k++) {
                    IloIntervalVar alt = cp.intervalVar();
                    alt.setOptional();
                    modes[i].add(alt);
                }
                cp.add(cp.alternative(task, modes[i].toArray()));
                ends.add(cp.endOf(task));
                for (int s = 0; s < nbSucc; s++) {
                    int succ = (int) data.next();
                    cp.add(cp.endBeforeStart(task, tasks[succ-1]));
                }
            }
            for (int i = 0; i < nbTasks; i++) {
                IloIntervalVar task = tasks[i];
                IntervalVarList imodes = modes[i];
                int taskId = (int) data.next();
                for(int k=0; k < imodes.size(); k++) {
//                    int taskId = data.next();
                    int modeId = (int) data.next();
                    int d = (int) data.next();
                    imodes.get(k).setSizeMin(d);
                    imodes.get(k).setSizeMax(d);
                    int q;
                    for (int j = 0; j < nbRenewable; j++) {
                        q = (int) data.next();
                        if (0 < q) {
                            renewables[j] = cp.sum(renewables[j], cp.pulse(imodes.get(k), q));
                        }
                    }
                    for (int j = 0; j < nbNonRenewable; j++) {
                        q = (int) data.next();
                        if (0 < q) {
                            nonRenewables[j] = cp.sum(nonRenewables[j], cp.prod(q, cp.presenceOf(imodes.get(k))));
                        }
                    }
                }
            }

            for (int j = 0; j < nbRenewable; j++) {
                cp.add(cp.le(renewables[j], capRenewables[j]));
            }

            for (int j = 0; j < nbNonRenewable; j++) {
                cp.add(cp.le(nonRenewables[j], capNonRenewables[j]));
            }

            IloObjective objective = cp.minimize(cp.max(arrayFromList(ends)));
            cp.add(objective);

            cp.setParameter(IloCP.IntParam.FailLimit, failLimit);
            System.out.println("Instance \t: " + projectFileName);
            if (cp.solve()) {
                System.out.println("Makespan \t: " + cp.getObjValue());
//                System.out.println(duedate);
            }
            else {
                System.out.println("No solution found.");
            }
        } catch (IloException e) {
            System.err.println("Error: " + e);
        }
    }



}
