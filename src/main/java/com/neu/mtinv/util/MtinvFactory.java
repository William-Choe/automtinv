package com.neu.mtinv.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class MtinvFactory{
    @Resource
    private FileUtil fu;

    @Resource
    private UserDate ud;

    public void create_jieya(String RAWDATAPath, String seedName) throws IOException {
        File jieya = new File("/jopens/AutoExport/jieya.sh");
        jieya.createNewFile();
        String filein_jieya = "#!/bin/bash\n" ;
        filein_jieya += "bash /jopens/AutoExport/Seed2Sac.sh  /jopens/AutoExport/" + seedName + " " + RAWDATAPath + "\n";
        RandomAccessFile mm_jieya;
        try {
            mm_jieya = new RandomAccessFile(jieya, "rw");
            mm_jieya.writeBytes(filein_jieya);
            mm_jieya.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_addinfounpackmtinv_manual(String RAWDATAPath, String lon, String lat, String depth, String seedName) throws IOException {
        File addinfonpackmtinv = new File(RAWDATAPath + "addinfounpackmtinv.sh");
        addinfonpackmtinv.createNewFile();
        String filein = "#!/bin/bash\n" ;
        filein += "# move files\n";
        filein += "cd " + RAWDATAPath + "\n";
        filein += "eventlo='"+ lon +"'\n";
        filein += "eventla='"+ lat +"'\n";
        filein += "eventdp='"+ depth +"'\n";
        filein += "csh " + RAWDATAPath + "unpack.csh " + seedName + "\n";
        filein += "#\n";
        filein += "while read line1 line2 line3 line4 line5\n";
        filein += "do\n";
        filein += "echo $line1 $line2 $line3 $line4 $line5\n";
        filein += "#\n";
        filein += "sac << EOF\n";
        filein += "r *.*.*.*.*.*.$line2.$line1.00.BH*.D.SAC\n";
        filein += "ch stla $line3\n";
        filein += "ch stlo $line4\n";
        filein += "ch evlo $eventlo\n";
        filein += "ch evla $eventla\n";
        filein += "ch evdp $eventdp\n";
        filein += "ch lcalda ture\n";
        filein += "ch lovrok ture\n";
        filein += "wh\n";
        filein += "quit\n";
        filein += "EOF\n";
        //2018-02-08 HHE HHN HHZ
        filein += "#\n";
        filein += "sac << EOF\n";
        filein += "r *.*.*.*.*.*.$line2.$line1.00.HH*.D.SAC\n";
        filein += "ch stla $line3\n";
        filein += "ch stlo $line4\n";
        filein += "ch evlo $eventlo\n";
        filein += "ch evla $eventla\n";
        filein += "ch evdp $eventdp\n";
        filein += "ch lcalda ture\n";
        filein += "ch lovrok ture\n";
        filein += "wh\n";
        filein += "quit\n";
        filein += "EOF\n";
        filein += "\n";
        filein += "ans=`ls -l *.$line1.00.*.SAC | grep \"^-\" | wc -l`\n";
        filein += "echo $ans\n";
        filein += "if [ $ans != 3 ]\n";
        filein += "then\n";
        filein += "rm -rf *.$line1.00.*.SAC\n";
        filein += "fi\n";
        filein += "done < " + RAWDATAPath + "rdseed.stations\n";
        filein += "sac << EOF\n";
        filein += "r *.*.*.*.*.*.*.*.00.BHZ.D.SAC\n";
        filein += "ch cmpaz 0\n";
        filein += "ch cmpinc 0\n";
        filein += "w over\n";
        filein += "r *.*.*.*.*.*.*.*.00.BHE.D.SAC\n";
        filein += "ch cmpaz 90\n";
        filein += "ch cmpinc 90\n";
        filein += "w over\n";
        filein += "r *.*.*.*.*.*.*.*.00.BHN.D.SAC\n";
        filein += "ch cmpaz 0\n";
        filein += "ch cmpinc 90\n";
        filein += "w over\n";
        filein += "quit\n";
        filein += "EOF\n";
        //2018-02-08 HHZ HHE HHN
        filein += "sac << EOF\n";
        filein += "r *.*.*.*.*.*.*.*.00.HHZ.D.SAC\n";
        filein += "ch cmpaz 0\n";
        filein += "ch cmpinc 0\n";
        filein += "w over\n";
        filein += "r *.*.*.*.*.*.*.*.00.HHE.D.SAC\n";
        filein += "ch cmpaz 90\n";
        filein += "ch cmpinc 90\n";
        filein += "w over\n";
        filein += "r *.*.*.*.*.*.*.*.00.HHN.D.SAC\n";
        filein += "ch cmpaz 0\n";
        filein += "ch cmpinc 90\n";
        filein += "w over\n";
        filein += "quit\n";
        filein += "EOF\n";
        filein += "echo 'Done!'";
        RandomAccessFile mm_add;
        try {
            mm_add = new RandomAccessFile(addinfonpackmtinv, "rw");
            mm_add.writeBytes(filein);
            mm_add.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_addinfounpackmtinv_auto(String RAWDATAPath, String lon, String lat, String depth, String seedName) throws IOException {
        File addinfonpackmtinv = new File(RAWDATAPath + "addinfounpackmtinv.sh");
        addinfonpackmtinv.createNewFile();
        String filein = "#!/bin/bash\n" ;
        filein += "# move files\n";
        filein += "cd " + RAWDATAPath + "\n";
        filein += "eventlo='"+ lon +"'\n";
        filein += "eventla='"+ lat +"'\n";
        filein += "eventdp='"+ depth +"'\n";
        filein += "csh " + RAWDATAPath + "unpack.csh " + seedName + "\n";
        filein += "#\n";

        filein += "while read line1 line2 line3 line4 line5\n";
        filein += "do\n";
        filein += "echo $line1 $line2 $line3 $line4 $line5\n";
        filein += "#\n";
        filein += "sac << EOF\n";
        filein += "r *.*.*.*.*.*.$line2.$line1.00.BH*.D.SAC\n";
        filein += "ch stla $line3\n";
        filein += "ch stlo $line4\n";
        filein += "ch evlo $eventlo\n";
        filein += "ch evla $eventla\n";
        filein += "ch evdp $eventdp\n";
        filein += "ch lcalda true\n";
        filein += "ch lovrok true\n";
        filein += "ch khole 00\n";
        filein += "w over\n";
        filein += "quit\n";
        filein += "EOF\n";
        //2018-02-08 HHZ HHE HHN
        filein += "sac << EOF\n";
        filein += "r *.*.*.*.*.*.$line2.$line1.00.HH*.D.SAC\n";
        filein += "ch stla $line3\n";
        filein += "ch stlo $line4\n";
        filein += "ch evlo $eventlo\n";
        filein += "ch evla $eventla\n";
        filein += "ch evdp $eventdp\n";
        filein += "ch lcalda true\n";
        filein += "ch lovrok true\n";
        filein += "ch khole 00\n";
        filein += "w over\n";
        filein += "quit\n";
        filein += "EOF\n";
        filein += "\n";
        filein += "ans=`ls -l *.$line1.00.*.SAC | grep \"^-\" | wc -l`\n";
        filein += "echo $ans\n";
        filein += "if [ $ans != 3 ]\n";
        filein += "then\n";
        filein += "rm -rf *.$line1.00.*.SAC\n";
        filein += "fi\n";
        filein += "done < " + RAWDATAPath + "rdseed.stations\n";
        filein += "sac << EOF\n";
        filein += "r *.*.*.*.*.*.*.*.00.BHZ.D.SAC\n";
        filein += "ch cmpaz 0\n";
        filein += "ch cmpinc 0\n";
        filein += "w over\n";
        filein += "r *.*.*.*.*.*.*.*.00.BHE.D.SAC\n";
        filein += "ch cmpaz 90\n";
        filein += "ch cmpinc 90\n";
        filein += "w over\n";
        filein += "r *.*.*.*.*.*.*.*.00.BHN.D.SAC\n";
        filein += "ch cmpaz 0\n";
        filein += "ch cmpinc 90\n";
        filein += "w over\n";
        filein += "quit\n";
        filein += "EOF\n";
        //2018-02-08 HHZ HHE HHN
        filein += "sac << EOF\n";
        filein += "r *.*.*.*.*.*.*.*.00.HHZ.D.SAC\n";
        filein += "ch cmpaz 0\n";
        filein += "ch cmpinc 0\n";
        filein += "w over\n";
        filein += "r *.*.*.*.*.*.*.*.00.HHE.D.SAC\n";
        filein += "ch cmpaz 90\n";
        filein += "ch cmpinc 90\n";
        filein += "w over\n";
        filein += "r *.*.*.*.*.*.*.*.00.HHN.D.SAC\n";
        filein += "ch cmpaz 0\n";
        filein += "ch cmpinc 90\n";
        filein += "w over\n";
        filein += "quit\n";
        filein += "EOF\n";
        RandomAccessFile mm_add;
        try {
            mm_add = new RandomAccessFile(addinfonpackmtinv, "rw");
            mm_add.writeBytes(filein);
            mm_add.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_cho(String RAWDATAPath, String UTCTime, String OTCTime, String lon, String lat, String depth) throws IOException {
        File cho = new File(RAWDATAPath + "cho.sm");
        cho.createNewFile();
        String filein_cho = "r $1\n" ;
        filein_cho += "ch nzjday "+ OTCTime +"\n";
        filein_cho += "ch evlo "+ lon +" evla "+ lat +"\n";
        filein_cho += "ch evdp "+ depth +"\n";
        filein_cho += "ch o gmt "+ UTCTime.substring(0,4) +" " + OTCTime + " " + UTCTime.substring(11,13) + " " + UTCTime.substring(14,16) + " " + UTCTime.substring(17,19) +" 000\n";
        filein_cho += "evaluate to tt1 &1,o * -1\n";
        filein_cho += "ch allt %tt1\n";
        filein_cho += "w over";
        RandomAccessFile mm_cho;
        try {
            mm_cho = new RandomAccessFile(cho, "rw");
            mm_cho.writeBytes(filein_cho);
            mm_cho.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_cutdata(String RAWDATAPath) throws IOException {
        File cutdata = new File(RAWDATAPath+"cutdata.cmd");
        cutdata.createNewFile();
        String filein_cut = "ls -1 " + RAWDATAPath + "*.SAC|awk '{print \"m " + RAWDATAPath + "cho.sm\",$1;print \"cut off\";\n" ;
        filein_cut += "print \"cut 0 300\";print \"r\",$1;\n";
        filein_cut += "print \"w over\"; print \"cut off\";}\n";
        filein_cut += "END{print \"q\";}'|sac\n";

        RandomAccessFile mm_cut;
        try {
            mm_cut = new RandomAccessFile(cutdata, "rw");
            mm_cut.writeBytes(filein_cut);
            mm_cut.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_syn(String RAWDATAPath) throws IOException {
        File syn = new File(RAWDATAPath+"syn.sh");
        syn.createNewFile();
        String filein_syn = "#!/bin/bash\n" ;
        filein_syn += "sac << EOF\n";
        filein_syn += "r " + RAWDATAPath + "*.SAC\n";
        filein_syn += "synch\n";
        filein_syn += "wh\n";
        filein_syn += "quit\n";
        filein_syn += "EOF";

        RandomAccessFile mm_syn;
        try {
            mm_syn = new RandomAccessFile(syn, "rw");
            mm_syn.writeBytes(filein_syn);
            mm_syn.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_unpack_manual(String RAWDATAPath) throws IOException {
        File unpack = new File(RAWDATAPath + "unpack.csh");
        unpack.createNewFile();
        String filein_unpack = "#!/bin/csh\n" ;
        filein_unpack += "## run rdseed to unpack station file\n";
        filein_unpack += "rdseed -p -Sf $1\n";
        filein_unpack += "rdseed -df $1\n";
        filein_unpack += "mkdir " + RAWDATAPath + "Resp\n";
        filein_unpack += "mv SAC_PZs* " + RAWDATAPath + "Resp\n";

        RandomAccessFile mm_unpack;
        try {
            mm_unpack = new RandomAccessFile(unpack, "rw");
            mm_unpack.writeBytes(filein_unpack);
            mm_unpack.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_unpack_auto(String RAWDATAPath) throws IOException {
        File unpack = new File(RAWDATAPath + "unpack.csh");
        unpack.createNewFile();
        String filein_unpack = "#!/bin/csh\n" ;
        filein_unpack += "## run rdseed to unpack station file\n";
        filein_unpack += "rdseed -p -Sf $1\n";
        filein_unpack += "mkdir " + RAWDATAPath + "Resp\n";
        filein_unpack += "mv SAC_PZs* " + RAWDATAPath + "Resp\n";
        RandomAccessFile mm_unpack;
        try {
            mm_unpack = new RandomAccessFile(unpack, "rw");
            mm_unpack.writeBytes(filein_unpack);
            mm_unpack.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_IDODISH(String SACPath, String RAWDATAPath, String distance_min, String distance_max) throws IOException {
        File idodist = new File(SACPath+"IDODIST.sh");
        idodist.createNewFile();
        String filein_idodist = "#!/bin/sh\n" ;
        filein_idodist += "DMIN=" + distance_min + "\n";
        filein_idodist += "DMAX=" + distance_max + "\n";
        filein_idodist += "cd " + RAWDATAPath + "\n";
        filein_idodist += "saclst KSTNM KNETWK DIST f *.SAC > sachdr.txt\n";
        filein_idodist += "while read line1 line2 line3 line4\n";
        filein_idodist += "do\n";
        filein_idodist += "echo $line1 $line2 $line3 $line4\n";
        filein_idodist += "ANS=`echo $line4 $DMIN $DMAX | awk '{if( $1 < $2)print \"NO\";else if($1 >$3)print \"NO\" ; else print \"YES\" }' `\n";
        filein_idodist += "if [ $ANS = \"YES\" ]\n";
        filein_idodist += "then\n";
        filein_idodist += "echo $i $line4 $ANS\n";
        filein_idodist += "cp $line1  " + SACPath + "\n";
        filein_idodist += "fi\n";
        filein_idodist += "done < sachdr.txt";
        RandomAccessFile mm_idodist;
        try {
            mm_idodist = new RandomAccessFile(idodist, "rw");
            mm_idodist.writeBytes(filein_idodist);
            mm_idodist.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_NETST(String SACPath) throws IOException {
        File netst = new File(SACPath+"NETST.sh");
        netst.createNewFile();
        String filein_netst = "#!/bin/sh\n" ;
        filein_netst += "cd " + SACPath + "\n";
        filein_netst += "saclst KSTNM KNETWK f *.BHZ.*.SAC >netstation.txt\n";
        filein_netst += "saclst KSTNM KNETWK f *.HHZ.*.SAC >>netstation.txt\n";
        RandomAccessFile mm_netst;
        try {
            mm_netst = new RandomAccessFile(netst, "rw");
            mm_netst.writeBytes(filein_netst);
            mm_netst.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_makeglib_manual(String MTINVPath, String RAWDATAPath, String SACPath, String UTCTime, String lat, String lon, String filter_min, String filter_max) throws IOException {
        File makeglib = new File(MTINVPath+"makeglib.csh");
        makeglib.createNewFile();
        String filein_ml = "#!/bin/csh\n" ;
        filein_ml += "cat>! wus.par << EOF\n";
        filein_ml += "velmod=wus\n";
        filein_ml += "zrange=2,2,20\n";
        filein_ml += "evla=" + lat + "\n";
        filein_ml += "evlo=" + lon + "\n";
        filein_ml += "dt=0.15\n";
        filein_ml += "nt=2048\n";
        filein_ml += "fmax=0.5\n";
        filein_ml += "t0=0\n";
        filein_ml += "redv=18\n";
        filein_ml += "damp=1.\n";
        filein_ml += "kmax=20000\n";
        filein_ml += "eps=0.0005\n";
        filein_ml += "smin=0.0005\n";
        filein_ml += "modeldb=/autoMTInv/modeldb\n";
        filein_ml += "stadb=" + RAWDATAPath + "rdseed.stations\n";
        filein_ml += "noverbose\n";
        filein_ml += "nodump\n";
        filein_ml += "EOF\n";
        filein_ml += "cat >! mkgrnlib.par << EOF\n";
        filein_ml += "#sta net par dt\n";

        String stationPath = SACPath + "netstation.txt";
        File file=new File(stationPath);
        if(file.isFile() && file.exists()){ //判断文件是否存在
            InputStreamReader read = new InputStreamReader(new FileInputStream(file));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while((lineTxt = bufferedReader.readLine()) != null){
                String lineArr[] = lineTxt.split("\\s+");
                if(lineArr.length == 3){
                    filein_ml += lineArr[1]+" "+lineArr[2]+" wus.par 0.1\n";
                }
            }
            read.close();
        }

        filein_ml += "EOF\n";
        filein_ml += "### Parallel version\n";
        filein_ml += "####\n";
        filein_ml += "multithread_mkgrnlib \\\n";
        filein_ml += "parfile=mkgrnlib.par \\\n";
        filein_ml += "executable_pathname=/opt/mtinv.v3.0.5/bin/mkgrnlib > \\\n";
        filein_ml += "multithread_mkgrnlib.out\n";

        String SACPath_temp = SACPath.substring(0,SACPath.length()-1);
        String RAWDATAPath_temp = RAWDATAPath.substring(0,RAWDATAPath.length());
        String maketime = UTCTime.substring(0, 4) + "/" + UTCTime.substring(5, 7) + "/" + UTCTime.substring(8, 10) + "," + UTCTime.substring(11, 19);
        filein_ml += "makepar com=\"SHAAN'XI Earthquake NetWork\" date=\"" + maketime + "\" DataDir=" + SACPath_temp + " RespDir=" + RAWDATAPath_temp + "Resp lf=" + filter_min + "  hf=" + filter_max + " *.glib\n";
        RandomAccessFile mm_ml;
        try {
            mm_ml = new RandomAccessFile(makeglib, "rw");
            mm_ml.writeBytes(filein_ml);
            mm_ml.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_makeglib_manual_batch(String MTINVPath, String RAWDATAPath, String SACPath, String UTCTime, String lat, String lon) throws IOException {
        File makeglib = new File(MTINVPath + "create_makeglib.csh");
        makeglib.createNewFile();
        String filein_ml = "#!/bin/csh\n" ;
        filein_ml += "cd " + MTINVPath + "\n";
        filein_ml += "cat>! wus.par << EOF\n";
        filein_ml += "velmod=wus\n";
        filein_ml += "zrange=2,2,40\n";
        filein_ml += "evla=" + lat + "\n";
        filein_ml += "evlo=" + lon + "\n";
        filein_ml += "dt=0.1\n";
        filein_ml += "nt=2048\n";
        filein_ml += "fmax=0.5\n";
        filein_ml += "t0=0\n";
        filein_ml += "redv=18\n";
        filein_ml += "damp=1.\n";
        filein_ml += "kmax=20000\n";
        filein_ml += "eps=0.0005\n";
        filein_ml += "smin=0.0005\n";
        filein_ml += "modeldb=/autoMTInv/modeldb\n";
        filein_ml += "stadb=" + RAWDATAPath + "rdseed.stations\n";
        filein_ml += "noverbose\n";
        filein_ml += "nodump\n";
        filein_ml += "EOF\n";
        filein_ml += "cat >! mkgrnlib.par << EOF\n";
        filein_ml += "#sta net par dt\n";

        String stationPath = SACPath + "netstation.txt";
        File file = new File(stationPath);
        //判断文件是否存在
        if(file.isFile() && file.exists()){
            InputStreamReader read = new InputStreamReader(new FileInputStream(file));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while((lineTxt = bufferedReader.readLine()) != null){
                String[] lineArr = lineTxt.split("\\s+");
                if(lineArr.length == 3){
                    filein_ml += lineArr[1] + " "+lineArr[2] + " wus.par 0.1\n";
                }
            }
            read.close();
        }

        filein_ml += "EOF\n";
        filein_ml += "## build makeglib.csh ##\n";
        filein_ml += "@ argNum = $#argv / 2\n";
        filein_ml += "@ iNum   = 1\n";
        filein_ml += "while ($iNum <= $argNum)\n";
        filein_ml += "  @ lf = $iNum * 2 - 1\n";
        filein_ml += "  @ hf = $iNum * 2\n\n";
        filein_ml += "  mkdir $argv[$lf]-$argv[$hf]    #folder name\n";
        filein_ml += "  cd $argv[$lf]-$argv[$hf]\n\n";
        filein_ml += "  cp ../wus.par ../mkgrnlib.par ./\n\n";
        filein_ml += "  ### Parallel version\n";
        filein_ml += "  cat >! makeglib.csh << EOF\n";
        filein_ml += "multithread_mkgrnlib \\\n";
        filein_ml += "parfile=mkgrnlib.par \\\n";
        filein_ml += "executable_pathname=/opt/mtinv.v3.0.5/bin/mkgrnlib > \\\n";
        filein_ml += "multithread_mkgrnlib.out\n";

        String SACPath_temp = SACPath.substring(0,SACPath.length()-1);
        String RAWDATAPath_temp = RAWDATAPath.substring(0,RAWDATAPath.length());
        String maketime = UTCTime.substring(0, 4) + "/" + UTCTime.substring(5, 7) + "/" + UTCTime.substring(8, 10) + "," + UTCTime.substring(11, 19);
        filein_ml += "makepar com=\"SHAAN'XI Earthquake NetWork\" date=\"" + maketime + "\" DataDir=" + SACPath_temp + " RespDir=" + RAWDATAPath_temp + "Resp lf=$argv[$lf]  hf=$argv[$hf] *.glib\n";
        filein_ml += "EOF\n";
        filein_ml += "  csh makeglib.csh\n";
        filein_ml += "  cd ../\n";
        filein_ml += "  @ iNum++\n";
        filein_ml += "end\n";

        RandomAccessFile mm_ml;
        try {
            mm_ml = new RandomAccessFile(makeglib, "rw");
            mm_ml.writeBytes(filein_ml);
            mm_ml.close();
        } catch (IOException e) {
            log.error("error occurs: ", e);
        }
    }

    public void create_makeglib_auto(String MTINVPath, String RAWDATAPath, String SACPath, String UTCTime, String lat, String lon, String filter_min, String filter_max) throws IOException {
        File makeglib = new File(MTINVPath+"makeglib.csh");
        makeglib.createNewFile();
        String filein_ml = "#!/bin/csh\n" ;
        filein_ml += "cat>! wus.par << EOF\n";
        filein_ml += "velmod=wus\n";
        filein_ml += "zrange=2,2,40\n";
        filein_ml += "evla=" + lat + "\n";
        filein_ml += "evlo=" + lon + "\n";
        filein_ml += "dt=0.1\n";
        filein_ml += "nt=2048\n";
        filein_ml += "fmax=0.5\n";
        filein_ml += "t0=0\n";
        filein_ml += "redv=18\n";
        filein_ml += "damp=1.\n";
        filein_ml += "kmax=20000\n";
        filein_ml += "eps=0.0005\n";
        filein_ml += "smin=0.0005\n";
        filein_ml += "modeldb=/autoMTInv/modeldb\n";
        filein_ml += "stadb=" + RAWDATAPath + "rdseed.stations\n";
        filein_ml += "noverbose\n";
        filein_ml += "nodump\n";
        filein_ml += "EOF\n";

        filein_ml += "cat >! mkgrnlib.par << EOF\n";
        filein_ml += "#sta net par dt\n";

        String stationPath = SACPath + "netstation.txt";
        File file=new File(stationPath);
        if(file.isFile() && file.exists()){ //判断文件是否存在
            InputStreamReader read = new InputStreamReader(new FileInputStream(file));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while((lineTxt = bufferedReader.readLine()) != null){
                String lineArr[] = lineTxt.split("\\s+");
                if(lineArr.length == 3){
                    filein_ml += lineArr[1] + " " + lineArr[2] + " wus.par 0.1\n";
                }
            }
            read.close();
        }

        filein_ml += "EOF\n";
        filein_ml += "### Parallel version\n";
        filein_ml += "####\n";
        filein_ml += "multithread_mkgrnlib \\\n";
        filein_ml += "parfile=mkgrnlib.par \\\n";
        filein_ml += "executable_pathname=/opt/mtinv.v3.0.5/bin/mkgrnlib > \\\n";
        filein_ml += "multithread_mkgrnlib.out\n";

        String SACPath_temp = SACPath.substring(0,SACPath.length()-1);
        String RAWDATAPath_temp = RAWDATAPath.substring(0,RAWDATAPath.length());
        String maketime = UTCTime.substring(0, 4) + "/" + UTCTime.substring(5, 7) + "/" + UTCTime.substring(8, 10) + "," + UTCTime.substring(11, 19);
        filein_ml += "makepar com=\"SHAAN'XI Earthquake NetWork\" date=\"" + maketime + "\" DataDir=" + SACPath_temp
                + " RespDir=" + RAWDATAPath_temp + "Resp lf=" + filter_min + "  hf=" + filter_max + " *.glib\n";
        RandomAccessFile mm_ml;
        try {
            mm_ml = new RandomAccessFile(makeglib, "rw");
            mm_ml.writeBytes(filein_ml);
            mm_ml.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_wus(String uc1, String uc2, String uc3, String uc4, String mc1, String mc2, String mc3, String mc4, String lc1, String lc2, String lc3, String lc4) throws IOException {
        File wus = new File("/autoMTInv/modeldb/wus.mod");
        wus.createNewFile();
        String filein_wus= "";
        filein_wus += uc1 + " " + uc2 + " 500.00 " + uc3 + " 250.00 " + uc4 + "\n";
        filein_wus += mc1 + " " + mc2 + " 500.00 " + mc3 + " 250.00 " + mc4 + "\n";
        filein_wus += lc1 + " " + lc2 + " 500.00 " + lc3 + " 250.00 " + lc4 + "\n";
        filein_wus += "700.00  7.64  1000.00  4.29   500.00  3.19";
        RandomAccessFile mm_wus;
        try {
            mm_wus = new RandomAccessFile(wus, "rw");
            mm_wus.writeBytes(filein_wus);
            mm_wus.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_tar(String MTINVPath, String fileTime) throws IOException {
        File tar = new File(MTINVPath + "tar.sh");
        tar.createNewFile();
        String filein_tar= "#!/bin/sh\n" ;
        filein_tar += "cd " + MTINVPath + "\n";
        filein_tar += "mkdir " + MTINVPath + "result\n";
        filein_tar += "cp *.txt " + MTINVPath + "result\n";
        filein_tar += "cp *.jpg " + MTINVPath + "result\n";
        filein_tar += "tar cvf " + fileTime + ".tar result\n";
        RandomAccessFile mm_tar;
        try {
            mm_tar = new RandomAccessFile(tar, "rw");
            mm_tar.writeBytes(filein_tar);
            mm_tar.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public String create_batch_tar(String[] fileList) throws IOException {
        String rootPath = "/autoMTInv/compute/tmp/";
        new File(rootPath).mkdir();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        String tmpDir = rootPath + timeStr;

        File tar = new File(tmpDir + ".sh");
        tar.createNewFile();

        String filein_tar= "#!/bin/sh\n" ;
        filein_tar += "cd " + rootPath + "\n";
        filein_tar += "mkdir " + tmpDir + "\n";
        for (String file: fileList) {
            filein_tar += "cp " + file + " " + tmpDir + "\n";
        }
        filein_tar += "tar cvf " + timeStr + ".tar " + timeStr + "\n";

        RandomAccessFile mm_tar;
        try {
            mm_tar = new RandomAccessFile(tar, "rw");
            mm_tar.writeBytes(filein_tar);
            mm_tar.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }

        return timeStr;
    }

    public void create_tar_best(String MTINVPath, String fileTime, String result_label) throws IOException {
        File tar = new File(MTINVPath + "tar_best.sh");
        tar.createNewFile();
        String filein_tar= "#!/bin/bash\n" ;
        filein_tar += "cd " + MTINVPath + "result\n";
        filein_tar += "mkdir " + MTINVPath + "best_result\n";
        filein_tar += "cp *" + result_label + "* " + MTINVPath + "best_result\n";
        filein_tar += "cp gmtmap* " + MTINVPath + "best_result\n";
        filein_tar += "cp plotmech* " + MTINVPath + "best_result\n";
        filein_tar += "cp plotz* " + MTINVPath + "best_result\n";
        filein_tar += "cp results* " + MTINVPath + "best_result\n";
        filein_tar += "cd " + MTINVPath + "\n";
        filein_tar += "tar cvf " + fileTime + "_best.tar best_result\n";
        RandomAccessFile mm_tar;
        try {
            mm_tar = new RandomAccessFile(tar, "rw");
            mm_tar.writeBytes(filein_tar);
            mm_tar.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_GETPOINT(String lon, String lat) throws IOException {
        File getpoint = new File("/autoMTInv/modeldb/crust2.0/GETPOINT.sh");
        getpoint.createNewFile();
        String filein_getpoint= "#!/bin/sh\n" ;
        filein_getpoint += "cd /autoMTInv/modeldb/crust2.0\n";
        filein_getpoint += "./getCN2point << EOF\n";
        filein_getpoint += lat + " " + lon + "\n";
        filein_getpoint += "exit" + "\n";
        filein_getpoint += "EOF\n";
        RandomAccessFile mm_getpoint;
        try {
            mm_getpoint = new RandomAccessFile(getpoint, "rw");
            mm_getpoint.writeBytes(filein_getpoint);
            mm_getpoint.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_maxpvr(String MTINVPath) throws IOException {
        File maxpvr = new File(MTINVPath + "maxpvr.sh");
        maxpvr.createNewFile();
        String filein= "#!/bin/sh\n" ;
        filein += "cd " + MTINVPath + "\n";
        filein += "maxpvr_value_v2 " + MTINVPath + "\n";
        RandomAccessFile mm;
        try {
            mm = new RandomAccessFile(maxpvr, "rw");
            mm.writeBytes(filein);
            mm.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_gmtmap1(String MTINVPath) {
        try {
            File gmtFile = new File(MTINVPath + "gmtmap1.csh");
            gmtFile.createNewFile();
            InputStreamReader read = new InputStreamReader(new FileInputStream(gmtFile));
            BufferedReader bufferedReader = new BufferedReader(read);
            StringBuffer buf = new StringBuffer();
            String lineTxt = null;
            int i = 0;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                i++;
                buf = buf.append(lineTxt);
                buf = buf.append(System.getProperty("line.separator"));
                if (i == 7) {
                    String a = "cd " + MTINVPath;
                    buf = buf.append(a);
                    buf = buf.append(System.getProperty("line.separator"));
                }
            }

            read.close();

            FileOutputStream fos = new FileOutputStream(gmtFile);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();
        } catch (Exception e) {
            log.error("error occurs: ", e);
        }
    }

    public void create_111(String MTINVPath, String lon, String lat, String depth, String result_s1, String result_d1, String result_r1, String result_m) throws IOException {
        File cmt = new File(MTINVPath + "111.cmt");
        cmt.delete();
        cmt.createNewFile();
        String cmt111 = lon + " " + lat + " " + depth + " " + result_s1 + " " + result_d1 + " " + result_r1 + " " + result_m + " " + lon + " " + lat;
        RandomAccessFile mm_cmt;
        try {
            mm_cmt = new RandomAccessFile(cmt, "rw");
            mm_cmt.writeBytes(cmt111);
            mm_cmt.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }
    }

    public void create_draw(String MTINVPath, String ddd) {
        String drawPath = MTINVPath + "draw-map.bat";
        fu.copyFile("/autoMTInv/draw-map.bat", drawPath);
        fu.writeTxtByStr(drawPath, ddd);
        String drawpath_cd = "cd " + MTINVPath;
        fu.writeTxtByStr(drawPath, drawpath_cd);

        String mecaPath = MTINVPath + "draw-meca.bat";
        fu.copyFile("/autoMTInv/draw-meca.bat", mecaPath);
        fu.writeTxtByStr(mecaPath, ddd);
        String mecapath_cd = "cd " + MTINVPath;
        fu.writeTxtByStr(mecaPath, mecapath_cd);
    }

    public void create_word(String MTINVPath, String o_time, double lat_d, double lon_d, String depth,
                            String magnitude, String localName, String result_s1, String result_d1, String result_r1, String result_s2, String result_d2, String result_r2, String result_m) throws ParseException, IOException {
        Map<String, Object> param = new HashMap<>();

        DecimalFormat df = new DecimalFormat("0.00");
        String lat_word = df.format(lat_d);
        lat_word = (lat_d > 0) ? "北纬" + lat_word : "南纬" + lat_word;
        String lon_word = df.format(lon_d);
        lon_word = (lon_d > 0) ? "东经" + lon_word : "西经" + lon_word;

        param.put("${local}", localName);
        param.put("${m}", magnitude);
        param.put("${yyyy}", o_time.substring(0, 4));
        param.put("${mm}", o_time.substring(5, 7));
        param.put("${dd}", o_time.substring(8, 10));
        param.put("${hh}", o_time.substring(11, 13));
        param.put("${fz}", o_time.substring(14, 16));
        param.put("${lat}", lat_word);
        param.put("${lon}", lon_word);
        param.put("${zx1}", result_s1);
        param.put("${qj1}", result_d1);
        param.put("${hdj1}", result_r1);
        param.put("${zx2}", result_s2);
        param.put("${qj2}", result_d2);
        param.put("${hdj2}", result_r2);
        param.put("${m2}", result_m);
        param.put("${depth}", depth);
        param.put("${cctime}", ud.getCcDate());

        Map<String, Object> pic1 = new HashMap<>();
        pic1.put("width", 400);
        pic1.put("height", 400);
        pic1.put("type", "jpg");
        pic1.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(MTINVPath + "ditu.jpg"), true));
        param.put("${pic1}", pic1);

        Map<String, Object> pic2 = new HashMap<>();
        pic2.put("width", 400);
        pic2.put("height", 400);
        pic2.put("type", "jpg");
        pic2.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(MTINVPath + "meca.jpg"), true));
        param.put("${pic2}", pic2);

        CustomXWPFDocument doc = WordUtil.generateWord(param, "/autoMTInv/AUTOMTINVword.docx");
        FileOutputStream fopts = new FileOutputStream(MTINVPath + "result.docx");
        doc.write(fopts);
        fopts.close();
    }


    /**
     * 第一次修改run.csh
     * @param rootPath
     */
    public void changeRunCshFirstTime(String rootPath){
        String MTINVPath = rootPath + "MTINV/";
        String ccc = "cd " + MTINVPath;
        fu.writeTxtByStr(MTINVPath + "run.csh", ccc);
    }

    public void changeRunCshFirstTimeBatch(String rootPath, String dirName){
        String MTINVPath = rootPath + "MTINV/" + dirName;
        String ccc = "cd " + MTINVPath;
        fu.writeTxtByStr(rootPath + "MTINV/" + dirName + "/run.csh", ccc);
    }

    /**
     * 第二次修改run.csh
     *
     * @param rootPath
     */
    public void changeRunCshSecondTime(String rootPath){
        try{
            //读取snr.out
            List<String> list = new ArrayList<>();
            File snrFile = new File(rootPath + "MTINV/snr.out");
            InputStreamReader read = new InputStreamReader(new FileInputStream(snrFile));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                String[] ls = lineTxt.split("\\s+");
                // 9 10 11 12 13
                String a1 = ls[9];
                String a2 = ls[10];
                String a3 = ls[11];
                String a4 = ls[12];
                String a5 = ls[13];

                int i1 = bjForC(a1);
                int i2 = bjForC(a2);
                int i3 = bjForC(a3);
                int i4 = bjForC(a4);
                int i5 = bjForC(a5);

                if (i1 == 0 || i2 == 0 || i3 == 0 || i4 == 0 || i5 == 0) {
                    list.add(ls[1]);
                }
            }
            read.close();

            // 修改run.csh
            File runFile = new File(rootPath + "MTINV/run.csh");
            InputStreamReader runRead = new InputStreamReader(
                    new FileInputStream(runFile));
            BufferedReader runBufferedReader = new BufferedReader(runRead);
            StringBuffer runBuf = new StringBuffer();
            String runLineTxt = null;
            String ttt = null;
            boolean temp = true;
            while ((runLineTxt = runBufferedReader.readLine()) != null) {
                runBuf = runBuf.append(runLineTxt);
                runBuf = runBuf.append(System.getProperty("line.separator"));

                if (runLineTxt != null) {
                    if (runLineTxt.indexOf("# sta") >= 0) {
                        while ((ttt = runBufferedReader.readLine()) != null) {
                            if (ttt.indexOf("EOF") >= 0) {
                                runBuf = runBuf.append(ttt);
                                runBuf = runBuf.append(System
                                        .getProperty("line.separator"));
                                break;
                            }

                            String text[] = ttt.split("\t");
                            String station = text[0];

                            Iterator<String> itr = list.iterator();
                            while (itr.hasNext()) {
                                String nextObj = itr.next();

                                if (nextObj.equals(station)) {
                                    runBuf = runBuf.append("#" + ttt);
                                    runBuf = runBuf.append(System.getProperty("line.separator"));
                                    temp = false;
                                    break;
                                } else {
                                    temp = true;
                                }
                            }

                            if (temp) {
                                runBuf = runBuf.append(ttt);
                                runBuf = runBuf.append(System.getProperty("line.separator"));
                            }
                        }
                    }
                }
            }
            read.close();
            FileOutputStream fos = new FileOutputStream(runFile);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(runBuf.toString().toCharArray());
            pw.flush();
            pw.close();
        }catch(Exception e){
            log.error(e.getMessage());
        }
    }

    public void changeRunCshSecondTimeBatch(String rootPath, String dirName){
        try{
            //读取snr.out
            List<String> list = new ArrayList<>();
            File snrFile = new File(rootPath + "MTINV/" + dirName + "/snr.out");
            InputStreamReader read = new InputStreamReader(new FileInputStream(snrFile));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                String[] ls = lineTxt.split("\\s+");
                // 9 10 11 12 13
                String a1 = ls[9];
                String a2 = ls[10];
                String a3 = ls[11];
                String a4 = ls[12];
                String a5 = ls[13];

                int i1 = bjForC(a1);
                int i2 = bjForC(a2);
                int i3 = bjForC(a3);
                int i4 = bjForC(a4);
                int i5 = bjForC(a5);

                if (i1 == 0 || i2 == 0 || i3 == 0 || i4 == 0 || i5 == 0) {
                    list.add(ls[1]);
                }
            }
            read.close();

            // 修改run.csh
            File runFile = new File(rootPath + "MTINV/" + dirName + "/run.csh");
            InputStreamReader runRead = new InputStreamReader(new FileInputStream(runFile));
            BufferedReader runBufferedReader = new BufferedReader(runRead);
            StringBuffer runBuf = new StringBuffer();
            String runLineTxt = null;
            String ttt = null;
            boolean temp = true;
            while ((runLineTxt = runBufferedReader.readLine()) != null) {
                runBuf = runBuf.append(runLineTxt);
                runBuf = runBuf.append(System.getProperty("line.separator"));

                if (runLineTxt != null) {
                    if (runLineTxt.indexOf("# sta") >= 0) {
                        while ((ttt = runBufferedReader.readLine()) != null) {
                            if (ttt.indexOf("EOF") >= 0) {
                                runBuf = runBuf.append(ttt);
                                runBuf = runBuf.append(System
                                        .getProperty("line.separator"));
                                break;
                            }

                            String text[] = ttt.split("\t");
                            String station = text[0];

                            Iterator<String> itr = list.iterator();
                            while (itr.hasNext()) {
                                String nextObj = itr.next();

                                if (nextObj.equals(station)) {
                                    runBuf = runBuf.append("#" + ttt);
                                    runBuf = runBuf.append(System.getProperty("line.separator"));
                                    temp = false;
                                    break;
                                } else {
                                    temp = true;
                                }
                            }

                            if (temp) {
                                runBuf = runBuf.append(ttt);
                                runBuf = runBuf.append(System.getProperty("line.separator"));
                            }
                        }
                    }
                }
            }
            read.close();
            FileOutputStream fos = new FileOutputStream(runFile);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(runBuf.toString().toCharArray());
            pw.flush();
            pw.close();
        }catch(Exception e){
            log.error(e.getMessage());
        }
    }

    public static int bjForC(String t){
        int result = 1;
        if (t.indexOf("nan") >= 0) {
            result = 0;
        } else if (t.indexOf("inf") >= 0) {
            result = 0;
        } else {
            double t_i = Double.parseDouble(t);
            if (t_i == 0) {
                result = 0;
            }
        }

        return result;
    }

    /**
     * 生成shaixuan.sh
     *
     * @param rootPath
     * @return
     */
    public String makeShaiXuanSh(String rootPath) {
        String result = "";

        try {
            // 新建shaixuan.sh
            File sxFile = new File(rootPath + "MTINV/shaixuan.csh");
            sxFile.createNewFile();
            String filein_sx = "#!/bin/csh\n";
            filein_sx += "cd "+rootPath+"MTINV/\n";

            //读取run.csh
            File runFile = new File(rootPath + "MTINV/run.csh");
            InputStreamReader read = new InputStreamReader(new FileInputStream(runFile));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            String ttt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                if (lineTxt.indexOf("cat >! mtinv.par << EOF")>=0) {
                    filein_sx += "cat >! mtinv.par << EOF\n";
                    while ((ttt = bufferedReader.readLine()) != null) {
                        if (ttt.indexOf("EOF")==0) {
                            filein_sx += ttt + "\n";
                            break;
                        }
                        filein_sx += ttt + "\n";
                    }
                }
            }
            filein_sx += "### PROCESS GREENS FUNCTIONS ### \n";
            filein_sx += "/opt/mtinv.v3.0.5/bin/glib2inv par=mtinv.par noverbose parallel \n";
            filein_sx += "### PROCESS DATA ### \n";
            filein_sx += "/opt/mtinv.v3.0.5/bin/sacdata2inv par=mtinv.par path=" + rootPath + "SAC respdir=" + rootPath + "RAWDATA/Resp noverbose nodumpsac parallel\n";

            RandomAccessFile mm_sx = null;
            mm_sx = new RandomAccessFile(sxFile, "rw");
            mm_sx.writeBytes(filein_sx);
            mm_sx.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }

        return result;
    }

    public String makeShaiXuanShBatch(String rootPath, String dirName) {
        String result = "";

        try {
            // 新建shaixuan.sh
            File sxFile = new File(rootPath + "MTINV/" + dirName + "/shaixuan.csh");
            sxFile.createNewFile();
            String filein_sx = "#!/bin/csh\n";
            filein_sx += "cd " + rootPath + "MTINV/" + dirName + "\n";

            //读取run.csh
            File runFile = new File(rootPath + "MTINV/" + dirName + "/run.csh");
            InputStreamReader read = new InputStreamReader(new FileInputStream(runFile));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            String ttt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                if (lineTxt.indexOf("cat >! mtinv.par << EOF") >= 0) {
                    filein_sx += "cat >! mtinv.par << EOF\n";
                    while ((ttt = bufferedReader.readLine()) != null) {
                        if (ttt.indexOf("EOF")==0) {
                            filein_sx += ttt + "\n";
                            break;
                        }
                        filein_sx += ttt + "\n";
                    }
                }
            }

            filein_sx += "### PROCESS GREENS FUNCTIONS ### \n";
            filein_sx += "/opt/mtinv.v3.0.5/bin/glib2inv par=mtinv.par noverbose parallel \n";

            filein_sx += "### PROCESS DATA ### \n";
            filein_sx += "/opt/mtinv.v3.0.5/bin/sacdata2inv par=mtinv.par path=" + rootPath + "SAC respdir=" + rootPath + "RAWDATA/Resp noverbose nodumpsac parallel\n";

            RandomAccessFile mm_sx;
            mm_sx = new RandomAccessFile(sxFile, "rw");
            mm_sx.writeBytes(filein_sx);
            mm_sx.close();
        } catch (IOException e1) {
            log.error("error occurs: ", e1);
        }

        return result;
    }
}