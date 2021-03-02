import sys
sys.path.append("./")
from datetime import datetime
from datetime import timedelta
import locale
import time

offline = SuperMemoOfflineInterface()

def testPlus(arg1,arg2):
    return arg1+arg2

def testDateTime(study_date):
    ll = locale.getdefaultlocale()
    print("testDateTime000",ll,locale.LC_ALL)
    # locale.setlocale(locale.LC_TIME, '')
    tt = time.strptime("2020-01-01", "%Y-%m-%d")
    print("testDateTime111",tt)
    st = datetime.strptime(study_date, '%Y-%m-%d')
    print("testDateTime222",st)
    dt = timedelta(days=1)
    print("testDateTime333",dt)
    return st
