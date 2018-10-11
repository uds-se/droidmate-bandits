import multiprocessing as mp
import subprocess
import sys
import copy
import os

HYBRID_DM2 = "/Users/christiandegott/Documents/Master/hybrid2/build/libs/hybrid-DM2.jar"
APK_FOLDER = "../apks"

ACTION_LIMIT = 1000
RESET_EVERY = 100
NUMBER_RUNS = 10

def do_work(message, serial):
    apk, run = message
    print("Starting run %i of %s on %s" % (run, apk, serial))
    
    output = "%s/run%i/droidMate" % (apk, run)
    os.makedirs(output, exist_ok=True)
    apks = "%s/%s" % (APK_FOLDER, apk)
    
    command = "java -jar "
    command += HYBRID_DM2
    command += " --Exploration-deviceSerialNumber=%s" % serial
    command += " --Output-outputDir=%s" % output
    command += " --Exploration-apksDir=%s" % apks
    command += " --Selectors-actionLimit=%i" % ACTION_LIMIT
    command += " --Selectors-resetEvery=%i" % RESET_EVERY
    
    #print(command)
    p = subprocess.Popen(command.split(" "), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = p.communicate()
    ret = p.returncode
    # print("out: {} err: {}".format(out, err))
    print("Run %i of %s on %s finished with %i" % (run, apk, serial, ret))

def worker(serial):
    for item in iter(q.get, None):
      do_work(item, serial)
      q.task_done()
    q.task_done()

if __name__ == '__main__':
    if (len(sys.argv) < 2):
      print("Usage: python3 %s <deviceSerial1> .. [deviceSerialN]" % sys.argv[0])
      sys.exit()
    
    # Setup one child process per device
    q = mp.JoinableQueue()
    procs = []
    for device in sys.argv[1:]:
      procs.append(mp.Process(target=worker, args=(device,)))
      procs[-1].daemon = True
      procs[-1].start()

    # Queue tasks
    for apk in os.listdir(APK_FOLDER):
        if apk.endswith(".apk"):
            for run in range(NUMBER_RUNS):
                q.put((apk, run + 1))
            q.join()

    # Send termination commands
    for p in procs:
      q.put(None)
    q.join()

    for p in procs:
      p.join()

    print("Finished all tasks....")
    print("Active children:", mp.active_children())