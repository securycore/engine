package com.zerjioang.apkr.analysis.flow;


import apkr.external.modules.helpers.log4j.Log;
import apkr.external.modules.helpers.log4j.LoggerType;
import com.zerjioang.apkr.analysis.dynamicscan.machine.base.AbstractDVMThread;
import com.zerjioang.apkr.analysis.dynamicscan.machine.base.DalvikVM;
import com.zerjioang.apkr.analysis.dynamicscan.machine.base.struct.generic.IAtomClass;
import com.zerjioang.apkr.analysis.dynamicscan.machine.base.struct.generic.IAtomFrame;
import com.zerjioang.apkr.analysis.dynamicscan.machine.base.struct.generic.IAtomMethod;
import com.zerjioang.apkr.analysis.dynamicscan.machine.reader.DexClassReader;
import com.zerjioang.apkr.sdk.model.base.ApkrProject;
import com.zerjioang.apkr.sdk.model.base.AtomTimeStamp;
import com.zerjioang.apkr.temp.ApkrIntelligence;

import java.util.ArrayList;
import java.util.Vector;

public final strictfp class OpCodeCheckerWorker extends AbstractFlowWorker {

    private static final int[] codeCount = new int[instructions.length];
    private static int total = 0;

    public OpCodeCheckerWorker(ApkrProject project) {
        super(new DalvikVM(project), project);
    }

    public OpCodeCheckerWorker(final DalvikVM vm, ApkrProject project) {
        super(vm, project);
    }

    @Override
    public void preload() {
        Log.write(LoggerType.DEBUG, "WORKER: OpCodeCheckerWorker");
        this.setStatus(AbstractDVMThread.STATUS_NOT_STARTED);
        vm.setThreads(new Vector());
        vm.addThread(this);
        this.timestamp.start();
    }

    @Override
    public void run() throws Throwable {
    }

    @Override
    public void finish() {
        Log.write(LoggerType.DEBUG, "WORKER: OpCodeCheckerWorker FINISHED!");
        currentProject.setInstructionCount(total);
        currentProject.setOpCodesCount(codeCount);
        this.timestamp.stop();
        Log.write(LoggerType.TRACE, "OpCodeCheckerWorker execution time:\t" + this.timestamp.getFormattedDuration() + " ( " + this.timestamp.getDuration() + " ms )");
    }

    @Override
    public int getInitialArgumentCount(IAtomClass cls, IAtomMethod m) {
        return 0;
    }

    @Override
    public Object getInitialArguments(IAtomClass cls, IAtomMethod m) {
        return null;
    }

    @Override
    public IAtomClass[] getInitialDVMClass() {
        //only return developer class and skip known java jdk and android sdk classes
        IAtomClass[] alllist = DexClassReader.getInstance().getAllClasses();
        ArrayList<IAtomClass> developerClasses = new ArrayList<>();
        for (IAtomClass cls : alllist) {
            if (ApkrIntelligence.getInstance().isDeveloperClass(cls.getName()))
                developerClasses.add(cls);
        }
        return developerClasses.toArray(new IAtomClass[developerClasses.size()]);
    }

    @Override
    public IAtomMethod[] getInitialMethodToRun(IAtomClass dexClass) {
        return dexClass.getAllMethods();
    }

    @Override
    public AbstractDVMThread reset() {
        //reset 'thread' status
        this.setStatus(STATUS_NOT_STARTED);
        this.removeFrames();
        this.timestamp = new AtomTimeStamp();
        return this;
    }

    @Override
    public strictfp void execute(boolean endless) throws Throwable {

        IAtomFrame frame = getCurrentFrame();
        IAtomMethod method = frame.getMethod();

        int[] lowerCodes = method.getOpcodes();

        for (int idx : lowerCodes) {
            codeCount[idx]++;
            total++;
        }

    }
}