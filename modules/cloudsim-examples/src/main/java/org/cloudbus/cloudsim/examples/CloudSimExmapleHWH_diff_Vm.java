package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 1 datacenter
 * 2 host
 * each host 10 pe(cpu)
 * allocate 4 vm
 * 4 cloudlet
 **/

public class CloudSimExmapleHWH_diff_Vm {
    /**
     * cloudlet list
     **/
    private static List<Cloudlet> cloudletList;

    /**
     * vm list
     **/
    private static List<Vm> vmList;


    public static void main(String[] args) {
        Log.printLine("start processer");
        try {
            int num_user = 1;
            Calendar instance = Calendar.getInstance();
            boolean trace_flag = false;

            CloudSim.init(num_user, instance, trace_flag);

            Datacenter datacenter1 = createDatacenter("tianjin01");
            DatacenterBroker datacenterBroker1 = createBroker();
            int brokerId = datacenterBroker1.getId();

            //创建vm
            List<Vm> vmList = new ArrayList<Vm>();
            //VM description

            int vmid;
            int mips = 250;
            long size = 10000; //image size (MB)
            int ram = 2048; //vm memory (MB)
            long bw = 1000;
            int pesNumber = 1; //number of cpus
            String vmm = "Xen"; //VMM name

            // alt shift insert 竖直选中粘贴，用完记得 alt shift insert取消该模式
            Vm vm1= new Vm(0,brokerId, mips, 3, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            Vm vm2= new Vm(1,brokerId, mips, 2, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            Vm vm3= new Vm(2,brokerId, mips, 1, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            Vm vm4= new Vm(3,brokerId, mips, 3, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            Vm vm5= new Vm(4,brokerId, mips, 3, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmList.add(vm1);
            vmList.add(vm2);
            vmList.add(vm3);
            vmList.add(vm4);
            vmList.add(vm5);

            for (Vm vm : vmList) {
                System.out.println("VM列表："+vm.toString());
            }
            //把vmlist给代理
            datacenterBroker1.submitVmList(vmList);

            cloudletList = new ArrayList<Cloudlet>();
            //Cloudlet properties
            int id;
            long length = 40000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();
            for (int i = 0; i < 4; i++) {
                Cloudlet cloudlet1 = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
                cloudlet1.setUserId(brokerId);
                cloudletList.add(cloudlet1);
            }
            //submit cloudlet list to the broker
            datacenterBroker1.submitCloudletList(cloudletList);

            for (int i = 0; i < 4; i++) {
                datacenterBroker1.bindCloudletToVm(cloudletList.get(i).getCloudletId(),vmList.get(i).getId());
            }

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();
            // Final step: Print results when simulation is over
            List<Cloudlet> newList = datacenterBroker1.getCloudletReceivedList();
            CloudSim.stopSimulation();
            printCloudletList(newList);
            Log.printLine("CloudSimExampleHWH  finished!");


        } catch (Exception e) {
            e.printStackTrace();
            Log.print("unknow error");
        }
    }


    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<Host>();
        List<Pe> peList = new ArrayList<Pe>();
        int mips = 1000;
        for (int i = 0; i < 10; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        int ram = 8192; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;
        Log.printLine("start make 2 hosts");
        for (int i = 0; i < 2; i++) {
            //拷贝新的pelist
            List<Pe> pelistNew = peList.stream().collect(Collectors.toList());
            hostList.add(new Host(i, new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw), storage, pelistNew, new VmSchedulerTimeShared(pelistNew)));
        }
        for (Host host : hostList) {
            System.out.println("host列表");
            System.out.println(host.toString());
        }
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;        // the cost of using memory in this resource
        double costPerStorage = 0.001;    // the cost of using storage in this resource
        double costPerBw = 0.0;            // the cost of using bw in this resource
        List<Storage> storageList = new ArrayList<Storage>();
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            //VmAllocationPolicySimple_Mine(hostList)             VmAllocationPolicySimple(hostList)
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple_Mine(hostList),
                    storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return datacenter;
    }


    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }



    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
                Log.print("SUCCESS");

                Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }
}