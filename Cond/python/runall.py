import sys
import os
from set_evn import execute_cmd

PROJECTS = [('lang', '65'), ('math', '106'), ('chart', '26'), ('time', '27')]

XGB_CONFIGS = [1] # [1, 2, 3, 4, 5, 6, 'a', 'b']

JAVA_CMD = 'java -ea -Xms1g -Xmx11g -Dfile.encoding=UTF-8 ' \
           '-classpath /home/nightwish/program_files/jdk1.8.0_241/jre/lib/resources.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/rt.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/jsse.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/jce.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/charsets.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/jfr.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/ext/dnsns.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/ext/sunpkcs11.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/ext/jaccess.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/ext/nashorn.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/ext/localedata.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/ext/jfxrt.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/ext/sunjce_provider.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/ext/sunec.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/ext/cldrdata.jar:' \
           '/home/nightwish/program_files/jdk1.8.0_241/jre/lib/ext/zipfs.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/bin:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/javacsv.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.core.contenttype_3.4.200.v20140207-1251.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.core.jobs_3.6.1.v20141014-1248.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.core.resources_3.9.1.v20140825-1431.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.core.runtime_3.10.0.v20140318-2214.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.equinox.common_3.6.200.v20130402-1505.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.equinox.preferences_3.5.200.v20140224-1527.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.jdt.core_3.10.0.v20140902-0626.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.jdt.core.source_3.10.0.v20140902-0626.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.osgi_3.10.2.v20150203-1939.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.jdt.ui_3.12.1.v20160822-0645.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.ui.workbench_3.108.0.v20160602-1232.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/org.eclipse.swt.gtk.linux.x86_64_3.105.0.v20160603-0902.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/dom4j-1.6.1.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/log4j-1.2.17.jar:' \
           '/home/nightwish/program_files/eclipse/plugins/org.junit_4.12.0.v201504281640/junit.jar:' \
           '/home/nightwish/program_files/eclipse/plugins/org.hamcrest.core_1.3.0.v201303031735.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/jdom-2.0.6.jar:' \
           '/home/nightwish/workspace/eclipse/Condition/lib/ProInfo.jar' \
           ' edu.pku.sei.conditon.dedu.predall.PredAllExperiment '


def process_projects(proj, id):
    # change dir
    os.chdir('/home/nightwish/workspace/eclipse/Condition/bin/')
    print 'WORKING AT ', os.getcwd()

    # run PredAllExperiment
    cmd = JAVA_CMD + proj + ' ' + id
    execute_cmd(cmd)


def print_cfg():
    with open('../bin/config.ini', 'r') as f:
        print '>>>>>>>>>>>>>>> CONFIG.INI >>>>>>>>>>>>>>>>>>>>'
        for line in f.readlines():
            if len(line.strip()) != 0 and not line.strip().startswith('#'):
                print '\t' + line.strip()
        print '>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>'


if __name__ == '__main__':
    # copy folder config
    cp_cmd = 'cp -rf ../config/ ../bin/'
    execute_cmd(cp_cmd)

    for i in XGB_CONFIGS:
        # cp config file
        cp_cmd = 'cp -f ../config/all-configs/' + str(i) + '.ini ../bin/config.ini'
        execute_cmd(cp_cmd)

        for proj in PROJECTS:
            name = proj[0]
            id = proj[1]
            print ('\n################################## PROCESSING CONFIGURE %d ##################################' % i)
            print ('################################## PROCESSING %s_%s #################################' % (name, id))
            print_cfg()
            process_projects(name, id)
            print '################################################################################################'
