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
//            IloCumulFunctionExpr[] renewables = new IloCumulFunctionExpr[nbRenewable];
//            IloIntExpr[] nonRenewables = new IloIntExpr[nbNonRenewable];
           
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
//                renewables[j] = cp.cumulFunctionExpr();
            	capRenewables[j] = sumValue(capEntRen[j]);
            }
            for (int j = 0; j < nbNonRenewable; j++) {
//                nonRenewables[j] = cp.intExpr();
            	capNonRenewables[j] = sumValue(capEntNon[j]);
            }
            
            IloIntervalVar[] tasks = new IloIntervalVar[nbTasks];
            IntervalVarList[] modes = new IntervalVarList[nbTasks];
            for (int i = 0; i < nbTasks; i++) {
                tasks[i] = cp.intervalVar();
                modes[i] = new IntervalVarList();
            }
            
            Location[] locals = new Location[nbTasks];
            for (int i = 0; i < locals.length - 0; i++) {
				locals[i] = new Location();
			}
            
            IloNumExpr[] Radius = new IloNumExpr[nbTasks];
            for (int i = 0; i < Radius.length - 0; i++) {
				Radius[i] = cp.numExpr();
				
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
            
            IloNumExpr[] qualities = new IloNumExpr[nbTasks];	// quality for every service/task
            for (int i = 0; i < nbTasks; i++) {
				qualities[i] = cp.numExpr();
			}
//            IloIntExpr Amount = cp.intExpr();
            
            for (int i = 0; i < nbTasks; i++) {
            	
            	IloNumExpr[] local = new IloNumExpr[2];
            	for (int j = 0; j < local.length; j++) {
            		local[j] = cp.numExpr();
				}
            	
            	IloIntExpr[] entChosen = new IloIntExpr[enterprises.size()];
            	IloNumExpr[] entRadius = new IloNumExpr[enterprises.size()];
            	for (int eid = 0; eid < enterprises.size(); eid++) {
					entChosen[eid] = cp.intExpr();
					entRadius[eid] = cp.numExpr();
				}
            	
            	IntervalVarList imodes = modes[i];
                IloNumExpr quality = cp.numExpr();
                IloIntExpr amtTotal = cp.intExpr();
                int taskId = (int) data.next();
                for(int k=0; k < imodes.size(); k++) {
                    int modeId = (int) data.next();
                    int d = (int) data.next();	// duration
                    imodes.get(k).setSizeMin(d);
                    imodes.get(k).setSizeMax(d);
                    IloNumExpr tempQuality = cp.numExpr();
                    IloIntExpr tempAmtTotal = cp.intExpr();
                    
                    IloNumExpr tempX = cp.numExpr();
                	IloNumExpr tempY = cp.numExpr();
                	
                	IloIntExpr[] tempChosen = new IloIntExpr[enterprises.size()];
                	for (int j = 0; j < enterprises.size(); j++) {
						tempChosen[j] = cp.intExpr();
					}
                    
                    int q;	// resource required amount
                    for (int type = 0; type < nbRenewable; type++) {
                    	
						q = (int) data.next();
						if (q > 0) {
							IloIntExpr sumRequire = cp.intExpr();
							
							for (int eid : entRenewables[type].keySet()) {							
								int vmax = enterprises.get(eid).getResourceAmount().get(type);
								entRenewables[type].put(eid, cp.sum(entRenewables[type].get(eid), cp.pulse(imodes.get(k), 0, vmax)));
								IloIntExpr amtChosen = cp.heightAtStart(imodes.get(k), entRenewables[type].get(eid));
								double entQuality = enterprises.get(eid).getQuality();
								tempQuality = cp.sum(tempQuality,cp.prod(entQuality, amtChosen));
								sumRequire = cp.sum(sumRequire, amtChosen);
								tempAmtTotal = cp.sum(tempAmtTotal,amtChosen);
								
								tempX = cp.sum(tempX,cp.prod(amtChosen, enterprises.get(eid).getX()));
								tempY = cp.sum(tempY,cp.prod(amtChosen, enterprises.get(eid).getY()));
								
								tempChosen[eid] = cp.sum(tempChosen[eid],amtChosen);
								
								
							}
							cp.add(cp.eq(sumRequire, cp.prod(q, cp.presenceOf(imodes.get(k)))) ) ;
//							tempAmtTotal = cp.sum(tempAmtTotal,q);
							
						}						
					}
                    
                    for (int type = 0; type < nbNonRenewable; type++) {
						q = (int) data.next();
						if (q > 0) {
							IloIntExpr sumRequire = cp.intExpr();

	                    	for (int eid : entNonRenewables[type].keySet()) {
	                    		int vmax = capEntNon[type].get(eid);
	                    		IloIntVar amount = cp.intVar(0, vmax);
//	                    		IloIntVar amount = nonrenewableResource[i][type].get(eid);
	                    		double entQuality = enterprises.get(eid).getQuality();
								entNonRenewables[type].put(eid, cp.sum(entNonRenewables[type].get(eid),cp.prod(amount,cp.presenceOf(imodes.get(k)) )) );
								tempQuality = cp.sum(tempQuality, cp.prod(entQuality, amount));
								sumRequire = cp.sum(sumRequire, amount);
//								tempAmtTotal = cp.sum(tempAmtTotal,amount);
								
								tempX = cp.sum(tempX,cp.prod(amount, enterprises.get(eid).getX()));
								tempY = cp.sum(tempY,cp.prod(amount, enterprises.get(eid).getY()));
								
								tempChosen[eid] = cp.sum(tempChosen[eid],amount);
								
	                    	}
	                    	cp.add(cp.eq(sumRequire, cp.prod(q, cp.presenceOf(imodes.get(k))) ));
	                    	tempAmtTotal = cp.sum(tempAmtTotal,q);
						}                    	
					}
                    local[0] = cp.sum(local[0],cp.prod(cp.presenceOf(imodes.get(k)), tempX));
                    local[1] = cp.sum(local[1],cp.prod(cp.presenceOf(imodes.get(k)), tempY));
                    
                    quality = cp.sum(quality, cp.prod(tempQuality, cp.presenceOf(imodes.get(k))));
//                    quality = cp.sum(quality,tempQuality);
                    amtTotal = cp.sum(amtTotal,cp.prod(tempAmtTotal, cp.presenceOf(imodes.get(k))));
                    
                    for (int eid = 0; eid < enterprises.size(); eid++) {
//                    	cp.ifThen(cp.gt(tempChosen[eid], 0), cp.eq(entChosen[eid], 1));  // add?
                    	entChosen[eid] = cp.sum(entChosen[eid], cp.prod(tempChosen[eid],cp.presenceOf(imodes.get(k))));
//                    	IloNumExpr x = cp.prod(enterprises.get(eid).getX(),cp.prod(entChosen[eid], cp.presenceOf(imodes.get(k))));
//                    	IloNumExpr y = cp.prod(enterprises.get(eid).getY(),cp.prod(entChosen[eid], cp.presenceOf(imodes.get(k))));
//                    	entRadius[eid] = cp.sum(entRadius[eid],norm2(x, local[0], y, local[1]));
//                    	entCho[eid] = cp.prod(entChose)
					}
                    
                    
//                    amtTotal = cp.sum(amtTotal,tempAmtTotal);
                }
                if (i > 0 && i < nbTasks-1) {
                	qualities[i] = cp.quot(quality, amtTotal);
                	for (int j = 0; j < local.length; j++) {
						local[j] = cp.quot(local[j], amtTotal);
					}
                	locals[i].setR(local);
                	
                	IloIntVar[] chosen = new IloIntVar[enterprises.size()];
                    IloNumExpr chosenMax = cp.max(entChosen);
                	for (int eid = 0; eid < chosen.length; eid++) {
                		chosen[eid] = cp.intVar(0, 1);
//                    	chosen[eid] = cp. entChosen[eid];
    					cp.ifThen(cp.gt(entChosen[eid],0), cp.eq(chosen[eid], 1)); // add?
    					IloNumExpr x = cp.prod(enterprises.get(eid).getX(),chosen[eid]);
                    	IloNumExpr y = cp.prod(enterprises.get(eid).getY(),chosen[eid]);
//                    	entRadius[eid] = norm2(x, local[0], y, local[1]);
                    	
                    	IloNumExpr index = cp.numExpr();
                    	index = cp.sum(index,0.5);
                    	entRadius[eid] = cp.power(cp.sum(cp.square(cp.abs(cp.diff(x, local[0]))),cp.square(cp.abs(cp.diff(y, local[1])))),index);
            
    				}
                    
                    Radius[i] = cp.max(entRadius);
                	
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
            
            // quality obj
            IloNumExpr objQuality = cp.sum(qualities);
            objQuality = cp.prod(-1.0, objQuality);
            		
            // makespan obj
            IloIntExpr objMakespan = cp.max(arrayFromList(ends));
            
            // radius obj
            IloNumExpr objRad = cp.max(Radius);
            
            IloMultiCriterionExpr objs = cp.staticLex(objRad);
            IloObjective objective = cp.minimize(objs);
            cp.add(objective);

            cp.setParameter(IloCP.IntParam.FailLimit, failLimit);
            cp.setParameter(IloCP.DoubleParam.TimeLimit, 200);
            
            System.out.println("Instance \t: " + projectFileName);
            if (cp.solve()) {
                System.out.println("Makespan \t: " + cp.getObjValues()[0]);
//                System.out.println("Total Quality \t: " + cp.getObjValues()[1]);
                System.out.println("Radius \t: " + cp.getValue(objRad));
                for (int i = 0; i < nbTasks; i++) {
					System.out.println(cp.getValue(Radius[i]));
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
    
//    public static IloNumExpr norm2(IloNumExpr x1, IloNumExpr x2, IloNumExpr y1, IloNumExpr y2) throws IloException{
//    	IloNumExpr middle = cp.sum(cp.square(cp.diff(x1,x2)),cp.square(cp.diff(y1,y2)));
//    	
//    	return Sqrt(middle);
//    }
//    
//    public static IloNumExpr Sqrt(IloNumExpr x) throws IloException{
//    	IloNumExpr result = cp.numExpr(); 
//    	result = cp.sum(Math.sqrt(2)*duedate*2,result);
////    	for (int i = 0; i < 11; i++) {
////			result = cp.prod(0.5, cp.sum(result,cp.quot(x, result)));
////		}
//    	return result;
//    }

}
