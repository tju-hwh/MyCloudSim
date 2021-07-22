/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Vm;

/**
 * PeProvisionerSimple is an extension of {@link PeProvisioner} which uses a best-effort policy to
 * allocate virtual PEs to VMs: 
 * if there is available mips on the physical PE, it allocates to a virtual PE; otherwise, it fails. 
 * Each host's PE has to have its own instance of a PeProvisioner.
 *
 * 供应者
 * *PeProvisionerSimple是{@link PeProvisioner}的扩展，它使用尽力而为策略
 * *将虚拟PE分配给虚拟机：
 * *如果物理PE上有可用的mips，则分配给虚拟PE；否则，它就失败了。
 * *每个主机的PE都必须有自己的PeProvisioner实例。
 *
 * *PE 指的是 Process Element， 就是逻辑核心(logic core)，一个逻辑核心上可以跑一个线程。这个概念引出是由于现在有很多双线程的处理器(double-thread core)，可以一个核心运行两个完全不同的任务/线程, 一个当两个用，所以不能单单当成一个核了，就说一个核有两个PE。
 * 名词解释：
 * 1个PE可以跑1个线程(thread)。PE指的是硬件，线程是跑在PE上的软件。
 * 比如说因特尔的i7-4790K，就是4核8线程处理器，每个核有两个线程，也就是有8个PE。
 *
 *
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PeProvisionerSimple extends PeProvisioner {

	/** The PE map, where each key is a VM id and each value
         * is the list of PEs (in terms of their amount of MIPS) 
         * allocated to that VM. */
	private Map<String, List<Double>> peTable;

	/**
	 * Instantiates a new pe provisioner simple.
	 * 
	 * @param availableMips The total mips capacity of the PE that the provisioner can allocate to VMs. 
	 * 
	 * @pre $none
	 * @post $none
	 */
	public PeProvisionerSimple(double availableMips) {
		super(availableMips);
		setPeTable(new HashMap<String, ArrayList<Double>>());
	}

	@Override
	public boolean allocateMipsForVm(Vm vm, double mips) {
		return allocateMipsForVm(vm.getUid(), mips);
	}

	@Override
	public boolean allocateMipsForVm(String vmUid, double mips) {
		if (getAvailableMips() < mips) {
			return false;
		}

		List<Double> allocatedMips;

		if (getPeTable().containsKey(vmUid)) {
			allocatedMips = getPeTable().get(vmUid);
		} else {
			allocatedMips = new ArrayList<Double>();
		}

		allocatedMips.add(mips);

		setAvailableMips(getAvailableMips() - mips);
		getPeTable().put(vmUid, allocatedMips);

		return true;
	}

	@Override
	public boolean allocateMipsForVm(Vm vm, List<Double> mips) {
		int totalMipsToAllocate = 0;
		for (double _mips : mips) {
			totalMipsToAllocate += _mips;
		}

		if (getAvailableMips() + getTotalAllocatedMipsForVm(vm) < totalMipsToAllocate) {
			return false;
		}

		setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForVm(vm) - totalMipsToAllocate);

		getPeTable().put(vm.getUid(), mips);

		return true;
	}

	@Override
	public void deallocateMipsForAllVms() {
		super.deallocateMipsForAllVms();
		getPeTable().clear();
	}

	@Override
	public double getAllocatedMipsForVmByVirtualPeId(Vm vm, int peId) {
		if (getPeTable().containsKey(vm.getUid())) {
			try {
				return getPeTable().get(vm.getUid()).get(peId);
			} catch (Exception e) {
			}
		}
		return 0;
	}

	@Override
	public List<Double> getAllocatedMipsForVm(Vm vm) {
		if (getPeTable().containsKey(vm.getUid())) {
			return getPeTable().get(vm.getUid());
		}
		return null;
	}

	@Override
	public double getTotalAllocatedMipsForVm(Vm vm) {
		if (getPeTable().containsKey(vm.getUid())) {
			double totalAllocatedMips = 0.0;
			for (double mips : getPeTable().get(vm.getUid())) {
				totalAllocatedMips += mips;
			}
			return totalAllocatedMips;
		}
		return 0;
	}

	@Override
	public void deallocateMipsForVm(Vm vm) {
		if (getPeTable().containsKey(vm.getUid())) {
			for (double mips : getPeTable().get(vm.getUid())) {
				setAvailableMips(getAvailableMips() + mips);
			}
			getPeTable().remove(vm.getUid());
		}
	}

	/**
	 * Gets the pe map.
	 * 
	 * @return the pe map
	 */
	protected Map<String, List<Double>> getPeTable() {
		return peTable;
	}

	/**
	 * Sets the pe map.
	 * 
	 * @param peTable the peTable to set
	 */
	@SuppressWarnings("unchecked")
	protected void setPeTable(Map<String, ? extends List<Double>> peTable) {
		this.peTable = (Map<String, List<Double>>) peTable;
	}

}
