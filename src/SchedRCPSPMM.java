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
import ilog.concert.cppimpl.IloBoolVar;
import ilog.cp.*;

import java.io.*;
import java.util.*;

public class SchedRCPSPMM {
//    static IloCP cp = new IloCP();
//    static int duedate;
	
    private static class IntervalVarList extends ArrayList<IloIntervalVar> {
        public IloIntervalVar[] toArray() {
            return (IloIntervalVar[]) this.toArray(new IloIntervalVar[this.size()]);
        }
    }

    static IloIntExpr[] arrayFromList(List<IloIntExpr> list) {
        return (IloIntExpr[]) list.toArray(new IloIntExpr[list.size()]);
    }

    public static void solve(String[] args) throws IOException {
        int failLimit = 30000;
        int nbTasks, nbRenewable, nbNonRenewable;
        int duedate;
        
        int nbEnterprise;
        List<Enterprise> enterprises = new ArrayList<Enterprise>();
        String projectFileName = "data/test";
        String enterpriseFileName = "src/output";
        
        if (args.length > 1){
            projectFileName = args[0];
        	enterpriseFileName = args[1];
        }
        if (args.length > 2)
            failLimit = Integer.parseInt(args[2]);
        
        
        IloCP cp = new IloCP();
        
        
        
        
        DataReader entData = new DataReader(enterpriseFileName);
        
        try {
        	
			nbEnterprise =  entData.next();
			for (int i = 0; i < nbEnterprise; i++) {
				int idx =  entData.next();
				int x = entData.next();
				int y = entData.next();
				int quality = entData.next();
				Enterprise e = new Enterprise(x, y);
				e.setIndex(idx);
				e.setQuality(quality);
				
				int nbType =  entData.next();
				
				for (int j = 0; j < nbType; j++) {
					int type =  entData.next();
					int amount =  entData.next();
					int cost =  entData.next();
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
            nbTasks =  data.next();
            nbRenewable =  data.next();
            nbNonRenewable =  data.next();
            IloNumVar index = cp.numVar(0.5,0.5);
            /*
             *  renewable resource grouped according to their types. (nbRenewable)
             *  nonrenewable resource grouped according to their types. (nbNonRenewable)
             */
            Map<Integer,IloCumulFunctionExpr>[] entRenewables = new HashMap[nbRenewable];
            Map<Integer,IloIntExpr>[] entNonRenewables = new HashMap[nbNonRenewable];
            
            /*
             * use old var
             */
//            IloCumulFunctionExpr[] renewables = new IloCumulFunctionExpr[nbRenewable];
//            IloIntExpr[] nonRenewables = new IloIntExpr[nbNonRenewable];
           
            Map<Integer,Integer>[] capEntRen = new HashMap[nbRenewable];
            Map<Integer,Integer>[] capEntNon = new HashMap[nbNonRenewable];
            
            int oldAmount;
            
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
            
            duedate =  data.next();
            
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
//                renewables[j] = cp.cumulFunctionExpr();
            	capRenewables[j] = sumValue(capEntRen[j]);
            }
            for (int j = 0; j < nbNonRenewable; j++) {
//                nonRenewables[j] = cp.intExpr();
            	capNonRenewables[j] = sumValue(capEntNon[j]);
            }
            
            // variables
            IloIntervalVar[] tasks = new IloIntervalVar[nbTasks];
            IloIntervalVar[] setups = new IloIntervalVar[nbTasks];
//            IloNumExpr[] setups = new IloNumExpr[nbTasks];
            IntervalVarList[] modes = new IntervalVarList[nbTasks];
            Location[] locals = new Location[nbTasks];
            IloNumExpr[] Radius = new IloNumExpr[nbTasks];
            IloIntExpr[] qualities = new IloIntExpr[nbTasks];
            
            // inits
            for (int i = 0; i < nbTasks; i++) {
                tasks[i] = cp.intervalVar();
                modes[i] = new IntervalVarList();
                setups[i] = cp.intervalVar();
//                setups[i] = cp.numExpr();
                locals[i] = new Location();
                Radius[i] = cp.numExpr();
                qualities[i] = cp.intExpr();
            }
            
            // precedence constraints
            List<IloIntExpr> ends = new ArrayList<IloIntExpr>();
//            List<IloIntExpr> endss = new ArrayList<IloIntExpr>();
            for (int i = 0; i < nbTasks; i++) {
                IloIntervalVar task = tasks[i];
//                IloIntervalVar setup = setups[i];
                int d =  data.next();
                int nbModes =  data.next();
                int nbSucc =  data.next();
                for (int k = 0; k < nbModes; k++) {
                    IloIntervalVar alt = cp.intervalVar();
                    alt.setOptional();
                    modes[i].add(alt);
                }
                
                cp.add(cp.alternative(task, modes[i].toArray()));
                ends.add(cp.endOf(task));
//                endss.add(cp.endOf(setup));
                for (int s = 0; s < nbSucc; s++) {
                    int succ =  data.next();
                    cp.add(cp.endBeforeStart(task, setups[succ-1]));
//                    cp.addGe(cp.diff(cp.startOf(tasks[succ-1]),ends.get(i)),setups[i]);
                }
                
                cp.add(cp.endBeforeStart(setups[i], task));
//                if (i == 0 || i == nbTasks-1) {
//					setup.setSizeMax(0);
//					setup.setSizeMin(0);
//				}
                
            }
            
            // resource constraints            
            for (int i = 0; i < nbTasks; i++) {
            	
            	IloIntExpr[] local = new IloIntExpr[2];
            	for (int j = 0; j < local.length; j++) {
            		local[j] = cp.intExpr();
				}
            	
            	IloIntExpr[] entChosen = new IloIntExpr[enterprises.size()];
            	IloNumExpr[] entRadius = new IloNumExpr[enterprises.size()];
            	IloIntVar[]  entRadiuses = new IloIntVar[enterprises.size()];
            	for (int eid = 0; eid < enterprises.size(); eid++) {
					entChosen[eid] = cp.intExpr();
					entRadius[eid] = cp.intExpr();
					entRadiuses[eid] = cp.intVar(0,Integer.MAX_VALUE);
				}
            	
//            	IloIntervalVar setup = setups[i];
            	IntervalVarList imodes = modes[i];
                IloIntExpr quality = cp.intExpr();
                IloIntExpr amtTotal = cp.intExpr();
                
                int taskId =  data.next();
                for(int k=0; k < imodes.size(); k++) {
                    int modeId =  data.next();
                    int d =  data.next();	// duration
                    imodes.get(k).setSizeMin(d);
                    imodes.get(k).setSizeMax(d);
                    

                    
                    IloIntExpr tempQuality = cp.intExpr();
                    IloIntExpr tempAmtTotal = cp.intExpr();
                    
                    IloIntExpr tempX = cp.intExpr();
                	IloIntExpr tempY = cp.intExpr();
                	
                	IloIntExpr[] tempChosen = new IloIntExpr[enterprises.size()];
                	for (int j = 0; j < enterprises.size(); j++) {
						tempChosen[j] = cp.intExpr();
					}
                    
                    int q;	// resource required amount
                    for (int type = 0; type < nbRenewable; type++) {
                    	
						q =  data.next();
						if (q > 0) {
							IloIntExpr sumRequire = cp.intExpr();
							
							for (int eid : entRenewables[type].keySet()) {							
								int vmax = enterprises.get(eid).getResourceAmount().get(type);
								entRenewables[type].put(eid, cp.sum(entRenewables[type].get(eid), cp.pulse(imodes.get(k), 0, vmax)));
								IloIntExpr amtChosen = cp.heightAtStart(imodes.get(k), entRenewables[type].get(eid));
								int entQuality = enterprises.get(eid).getQuality();
								tempQuality = cp.sum(tempQuality,cp.prod(entQuality, amtChosen));
								sumRequire = cp.sum(sumRequire, amtChosen);
								
								tempX = cp.sum(tempX,cp.prod(amtChosen, enterprises.get(eid).getX()));
								tempY = cp.sum(tempY,cp.prod(amtChosen, enterprises.get(eid).getY()));
								
								tempChosen[eid] = cp.sum(tempChosen[eid],amtChosen);
								
								
							}
							cp.add(cp.eq(sumRequire, cp.prod(q, cp.presenceOf(imodes.get(k)))) ) ;
							tempAmtTotal = cp.sum(tempAmtTotal,q);
							
						}						
					}
                    
                    for (int type = 0; type < nbNonRenewable; type++) {
						q =  data.next();
						if (q > 0) {
							IloIntExpr sumRequire = cp.intExpr();

	                    	for (int eid : entNonRenewables[type].keySet()) {
	                    		int vmax = capEntNon[type].get(eid);
	                    		IloIntVar amount = cp.intVar(0, vmax);
	                    		int entQuality = enterprises.get(eid).getQuality();
								entNonRenewables[type].put(eid, cp.sum(entNonRenewables[type].get(eid),amount));
								tempQuality = cp.sum(tempQuality, cp.prod(entQuality, amount));
								sumRequire = cp.sum(sumRequire, amount);
								
								tempX = cp.sum(tempX,cp.prod(amount, enterprises.get(eid).getX()));
								tempY = cp.sum(tempY,cp.prod(amount, enterprises.get(eid).getY()));
								
								tempChosen[eid] = cp.sum(tempChosen[eid],amount);
								
	                    	}
	                    	cp.add(cp.eq(sumRequire, cp.prod(q, cp.presenceOf(imodes.get(k))) ));
	                    	tempAmtTotal = cp.sum(tempAmtTotal,q);
						}                    	
					}
                    local[0] = cp.sum(local[0],tempX);
                    local[1] = cp.sum(local[1],tempY);
                    
//                    quality = cp.sum(quality, cp.prod(tempQuality, cp.presenceOf(imodes.get(k))));
                    quality = cp.sum(quality, tempQuality);
                    amtTotal = cp.sum(amtTotal,cp.prod(tempAmtTotal, cp.presenceOf(imodes.get(k))));
                    
                    for (int eid = 0; eid < enterprises.size(); eid++) {
                    	entChosen[eid] = cp.sum(entChosen[eid], tempChosen[eid]);
                    }
                    
                    
                }
                

                if (i > 0 && i < nbTasks-1) {
                	qualities[i] = cp.div(quality, amtTotal);
                	for (int j = 0; j < local.length; j++) {
						local[j] = cp.div(local[j], amtTotal);
					}
                }
//                else{
//					qualities[i] = cp.numVar(0, 0);
//					local[0] = cp.numVar(0, 0);
//					local[1] = cp.numVar(0, 0);
//                	Radius[i] = cp.sum(Radius[i],0);
//				}
                
            	locals[i].setR(local);
                
                IloIntVar[] chosen = new IloIntVar[enterprises.size()];
                IloIntExpr chosenMax = cp.max(entChosen);
            	for (int eid = 0; eid < chosen.length; eid++) {
            		chosen[eid] = cp.intVar(0, 1);
					cp.add(cp.ifThenElse(cp.gt(entChosen[eid],0), cp.eq(chosen[eid], 1), cp.eq(chosen[eid], 0))); // add?
					
					IloIntExpr x = cp.prod(enterprises.get(eid).getX(),chosen[eid]);
                	IloIntExpr y = cp.prod(enterprises.get(eid).getY(),chosen[eid]);
                	
                	IloIntExpr cx = cp.prod(local[0],chosen[eid]);
                	IloIntExpr cy = cp.prod(local[1],chosen[eid]);
                	
//                	entRadius[eid] = cp.numVar(0, Double.MAX_VALUE)；
//                	entRadius[eid] = x;
//                	entRadius[eid] = cp.sum(cp.abs(cp.diff(x, cx)),cp.abs(cp.diff(y, cy)));
                	entRadius[eid] = cp.power(cp.sum(cp.square(cp.abs(cp.diff(x, cx))),cp.square(cp.abs(cp.diff(y, cy)))),index);
//                	cp.addLe(entRadiuses[eid] , cp.ceil(cp.power(cp.sum(cp.square(cp.abs(cp.diff(x, cx))),cp.square(cp.abs(cp.diff(y, cy)))),index)));
//                	cp.addGe(entRadiuses[eid] , cp.power(cp.sum(cp.square(cp.abs(cp.diff(x, cx))),cp.square(cp.abs(cp.diff(y, cy)))),index));
//                	entRadius[eid] = cp.ceil(cp.quot(entRadius[eid],5));
				}
                Radius[i] = cp.max(entRadius);
                IloIntVar zzz = cp.intVar(0, Integer.MAX_VALUE);
                cp.addGe(zzz, Radius[i]);
//                cp.addGe(setups[i],Radius[i]);
//                setups[i] = cp.ceil(cp.sum(Radius[i],setups[i]));
                cp.addGe(cp.diff(cp.endOf(setups[i]), cp.startOf(setups[i])),zzz);
//                cp.add(cp.ge(cp.endOf(setups[i]),cp.sum(cp.startOf(setups[i]),Radius[i])));
                
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
            
            // quality obj
            IloIntExpr objQuality = cp.sum(qualities);
            objQuality = cp.prod(-1, objQuality);
            		
            // makespan obj
            IloIntExpr objMakespan = cp.max(arrayFromList(ends));
            
            // radius obj
//            IloIntExpr objRad = cp.max(Radius);
            
            IloMultiCriterionExpr objs = cp.staticLex(objMakespan,objQuality);
            IloObjective objective = cp.minimize(objs);
            cp.add(objective);

//            cp.setParameter(IloCP.IntParam.FailLimit, failLimit);
//            cp.setParameter(IloCP.DoubleParam.TimeLimit, 200);
            
            System.out.println("Instance \t: " + projectFileName);
            if (cp.solve()) {
                System.out.println("Makespan \t: " + cp.getObjValues()[0]);
                System.out.println("Total Quality \t: " + cp.getObjValues()[1]);
//                System.out.println("Radius \t: " + cp.getValue(objRad));
                for (int i = 0; i < nbTasks; i++) {
					System.out.println(cp.getValue(cp.endOf(setups[i])));
				}
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
