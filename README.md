# CCDBtools3
Java classes for retrieving and analyzing constants from CCDB.
##Features
- Print a table for a given variation, run number, and date
- Get constants by their sector/layer/component indicies
- Plot a constant vs run number

# Documentation
[Javadoc](https://userweb.jlab.org/~nathanh/CLAS12softwareValidation/CCDBtools/doc/)

# Example code
```groovy
  1 import org.root.histogram.*;
  2 import org.root.pad.*;
  3 import ccdbTools.*;
  4 
  5 ConstantsComparer cc = new ConstantsComparer();
  6 
  7 cc.printTable("/calibration/dc/signal_generation/intrinsic_inefficiency", "default", 10, new Date());
  8 
  9 double a = cc.getConstantBySLC("/calibration/ftof/time_walk", "default", 10, new Date(), 3, 5, 2, 58);
 10 System.out.println(a);
 11 a = cc.getConstantBySLC("/calibration/ftof/time_walk", "default", 10, new Date(), 3, 2, 1, 15);
 12 System.out.println(a);
 13 a = cc.getConstantBySLC("/calibration/ftof/time_walk", "default", 10, new Date(), 3, 1, 3, 2);
 14 System.out.println(a);
 15 a = cc.getConstantBySLC("/calibration/ftof/time_walk", "default", 10, new Date(), 3, 4, 2, 55);
 16 System.out.println(a);
 17 
 18 GraphErrors gr = cc.getConstantRunDependenceBySLC("/calibration/ec/attenuation", "default", 0, 15, new Date(), 4, 5, 2, 58);
 19 GraphErrors gr2 = cc.getConstantRunDependenceBySLC("/calibration/ec/attenuation", "default", 2, 15, new Date(), 4, 1, 1, 12);
 20 GraphErrors gr3 = cc.getConstantRunDependenceByIndex("/calibration/forward_tagger/calorimeter/time", "default", 0, 15, new Date(), 2, 8);
 21 
 22 TGCanvas can = new TGCanvas("can", "can", 900, 300, 3, 1);
 23 can.cd(0);
 24 can.draw(gr);
 25 can.cd(1);
 26 can.draw(gr2);
 27 can.cd(2);
 28 can.draw(gr3);
```

The printout of this code will be something like:

```tcsh
ENVIRONMENT : /Users/harrison/coatjava-2.4/bin/.. null null null
DB address not defined in your environment.
Returning to default address: mysql://clas12reader@clasdb.jlab.org/clas12
1   1.25e-4   0.05   25.0e-4   0.15   0.0   
2   1.25e-4   0.05   25.0e-4   0.15   0.0   
3   1.25e-4   0.05   25.0e-4   0.15   0.0   
4   1.25e-4   0.05   25.0e-4   0.15   0.0   
5   1.25e-4   0.05   25.0e-4   0.15   0.0   
6   1.25e-4   0.05   25.0e-4   0.15   0.0   
50.0
50.0
50.0
50.0
```

and you should get three plots similar to these:

<img src="https://github.com/naharrison/CCDBtools3/blob/master/images/ccdbToolsExamplePlots.png" width="800">
