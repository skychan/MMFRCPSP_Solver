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
				
				enterprises.add(e);
				
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
            
            /*
             *  renewable resource grouped according to their types. (nbRenewable)
             *  nonrenewable resource grouped according to their types. (nbNonRenewable)
             */
            Map<Integer,IloCumulFunctionExpr>[] entRenewables = new HashMap[nbRenewable];
            Map<Integer,IloIntExpr>[] entNonRenewables = new HashMap[nbNonRenewable];
            
            /*
             * use old var
             */
            IloCumulFunctionExpr[] renewables = new IloCumulFunctionExpr[nbRenewable];
            IloIntExpr[] nonRenewables = new IloIntExpr[nbNonRenewable];
           
            Map<Integer,Integer>[] capEntRen = new HashMap[nbRenewable];
            Map<Integer,Integer>[] capEntNon = new HashMap[nbNonRenewable];
            
            double oldAmount;
            
            for (int type = 0; type < nbRenewable; type++) {
				capEntRen[type] = new HashMap<Integer, Integer>();
				entRenewables[type] = new HashMap<Integer, IloCumulFunctionExpr>();
				oldAmount = data.next();
			}
            for (int type = 0; type < nbNonRenewable; type++) {
				capEntNon[type] = new HashMap<Integer, Integer>();
				entNonRenewables[type] = new HashMap<Integer, IloIntExpr>();
				oldAmount = data.next();
			}
            
            duedate = (int) data.next();
            
            for (Enterprise ent : enterprises) {
            	for (int tid : ent.getResourceAmount().keySet() ) {
            		if (tid < nbRenewable) {
            			int type = tid;
            			capEntRen[type].put(ent.getIndex(), ent.getResourceAmount().get(tid));
            			entRenewables[type].put(ent.getIndex(),cp.cumulFunctionExpr());
					} else {
						int type = tid - nbRenewable;
						capEntNon[type].put(ent.getIndex(), ent.getResourceAmount().get(tid));
            			entNonRenewables[type].put(ent.getIndex(),cp.intExpr());
					}
				}
				
			}
            
            /*
             * use old caps
             */
            int[] capRenewables = new int[nbRenewable];            
            int[] capNonRenewables = new int[nbNonRenewable];
            

            for (int j = 0; j < nbRenewable; j++) {
                renewables[j] = cp.cumulFunctionExpr();
//                capRenewables[j] = (int) data.next();
            	capRenewables[j] = sumValue(capEntRen[j]);
            }
            for (int j = 0; j < nbNonRenewable; j++) {
                nonRenewables[j] = cp.intExpr();
//                capNonRenewables[j] = (int) data.next();
            	capNonRenewables[j] = sumValue(capEntNon[j]);
            }
//            
//            duedate = (int) data.next();
            
            IloIntervalVar[] tasks = new IloIntervalVar[nbTasks];
            IntervalVarList[] modes = new IntervalVarList[nbTasks];
            Map<Integer, IloIntervalVar>[][] renewableResource = new HashMap[nbTasks][nbRenewable];
			Map<Integer, IloIntVar>[][] nonrenewableResource = new HashMap[nbTasks][nbNonRenewable];
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
//                amtResource[i] = new HashMap[nbRenewable + nbNonRenewable];
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
                
                for (int type = 0; type < nbRenewable; type++) {
                	renewableResource[i][type] = new HashMap<Integer, IloIntervalVar>();
                	for (int eid : entRenewables[type].keySet()) {
                		IloIntervalVar amount = cp.intervalVar();
						renewableResource[i][type].put(eid, amount);
					}
				}
                
                for (int type = 0; type < nbNonRenewable; type++) {
					nonrenewableResource[i][type] = new HashMap<Integer, IloIntVar>();
//					System.out.println(entNonRenewables[type].keySet());
					for (int eid : entNonRenewables[type].keySet()) {
						
//						System.out.println(capEntNon[type]);
						int vmax = capEntNon[type].get(eid);
//						System.out.println(vmax);
						IloIntVar amount = cp.intVar(0, vmax);
						nonrenewableResource[i][type].put(eid, amount);
					}
				}
                
            }
            for (int i = 0; i < nbTasks; i++) {
//                IloIntervalVar task = tasks[i];
                IntervalVarList imodes = modes[i];
                int taskId = (int) data.next();
                for(int k=0; k < imodes.size(); k++) {
//                    int taskId = data.next();
                    int modeId = (int) data.next();
                    int d = (int) data.next();	// duration
                    imodes.get(k).setSizeMin(d);
                    imodes.get(k).setSizeMax(d);
                    int q;	// resource required amount
                    for (int type = 0; type < nbRenewable; type++) {
                    	
						q = (int) data.next();
						if (q > 0) {
//							renewables[type] = cp.sum(renewables[type], cp.pulse(imodes.get(k), q));
							IloIntExpr sumRequire = cp.intExpr();
							for (int eid : entRenewables[type].keySet()) {							
//								IloIntervalVar amount = renewableResource[i][type].get(eid);
//								amount.setSizeMax(imodes.get(k).getSizeMax());
//								amount.setSizeMin(imodes.get(k).getSizeMin());
								int vmax = enterprises.get(eid).getResourceAmount().get(type);
								entRenewables[type].put(eid, cp.sum(entRenewables[type].get(eid), cp.pulse(imodes.get(k), 0, vmax)));
								
								sumRequire = cp.sum(sumRequire, cp.heightAtStart(imodes.get(k), entRenewables[type].get(eid)) );
							}
							cp.add(cp.eq(sumRequire, cp.prod(q, cp.presenceOf(imodes.get(k)))) ) ;
//							sumRequire.sum(-sumRequire,);
//							cp.add(cp.eq(sumRequire, q ));
						}
						
						
						
					}
                    
                    for (int type = 0; type < nbNonRenewable; type++) {
						q = (int) data.next();
						if (q > 0) {
							IloIntExpr sumRequire = cp.intExpr();

	                    	for (int eid : entNonRenewables[type].keySet()) {
//								IloIntVar amount = nonrenewableResource[i][type].get(eid);
	                    		int vmax = capEntNon[type].get(eid);
	                    		IloIntVar amount = cp.intVar(0, vmax);
//								amount.setSizeMax(imodes.get(k).getSizeMax());
//								amount.setSizeMin(imodes.get(k).getSizeMin());
//								int vmax = enterprises.get(eid).getResourceAmount().get(type);
								entNonRenewables[type].put(eid, cp.sum(entNonRenewables[type].get(eid),cp.prod(amount,cp.presenceOf(imodes.get(k)) )) );
								
								sumRequire = cp.sum(sumRequire, amount);
	                    	}
	                    	cp.add(cp.eq(sumRequire, cp.prod(q, cp.presenceOf(imodes.get(k))) ));
						}
						
                    	
					}
                    
//                    for (int j = 0; j < nbRenewable; j++) {
//                        q = (int) data.next();
//                        if (0 < q) {
//                            renewables[j] = cp.sum(renewables[j], cp.pulse(imodes.get(k), q));
//                        }
//                    }
//                    for (int j = 0; j < nbNonRenewable; j++) {
//                        q = (int) data.next();
//                        if (0 < q) {
//                            nonRenewables[j] = cp.sum(nonRenewables[j], cp.prod(q, cp.presenceOf(imodes.get(k))));
//                        }
//                    }
                }
            }
            
            for(int type = 0; type < nbRenewable; type++){
            	for (int eid : entRenewables[type].keySet()) {
					cp.add(cp.le(entRenewables[type].get(eid),capEntRen[type].get(eid)));
				}
            }
            
            for(int type = 0; type < nbNonRenewable; type++){
            	for (int eid : entNonRenewables[type].keySet()) {
					cp.add(cp.le(entNonRenewables[type].get(eid),capEntNon[type].get(eid)));
				}
            }
//            entRenewables[type].get(eid) <= capEntRen[eid].get(eid)

//            for (int j = 0; j < nbRenewable; j++) {
//                cp.add(cp.le(renewables[j], capRenewables[j])); // renewable(expr) capRenewables(int[])
//            }
//
//            for (int j = 0; j < nbNonRenewable; j++) {
//                cp.add(cp.le(nonRenewables[j], capNonRenewables[j]));
//            }

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

    public static int sumValue(Map<Integer,Integer> map){
		int sum = 0;
		for (int v : map.values()) {
			sum += v;
		}
    	return sum;
    	
    }

}
