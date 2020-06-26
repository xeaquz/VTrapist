
# coding: utf-8

# In[94]:


import pandas as pd
import numpy as np
from matplotlib import pyplot as plt
from matplotlib import style
from scipy import signal
from scipy.signal import find_peaks

import obspy
from obspy.signal.detrend import polynomial
style.use('ggplot')

import warnings
warnings.simplefilter('ignore', np.RankWarning)


# In[107]:


class emotional_analysis :
    initial_user_max = 0 # max signal in initial user's data for applying adaptive threshold
    total_user = 0 # the number of users
    initialT = 0 # threshold of first user
    
    # initialize
    def __init__(self, sampling_rate, event_size, start_time, user_time):
        self.sampling_rate = sampling_rate # sampling rate
        self.event_size = event_size # the number of events
        self.start_time = start_time # video start time
        self.user_time = user_time # time when first biometric signal is detected about first event for each user
        self.user_order = emotional_analysis.total_user+1  # user_order = 1 --> initial user
        emotional_analysis.total_user += 1 # add new user
        
    def print_info(self):
        print("sampling rate :", self.sampling_rate, ", event size :",self.event_size,
             ", video start time :", self.start_time, ", user time:",self.user_time,
             ", user order :",self.user_order, ", total user :",emotional_analysis.total_user)

    def set_index_time(self) :
        event = np.zeros(self.event_size)

        for i in range(self.event_size) :
            if(i==0) : 
                event[i] = 9
            else :
                event[i] = event[i-1] + 16

        event = event.astype(np.int) # type conversion : floating -> integer
        print("* Data Event *")
        print(event)
        
        self.event = event


        pleasant = [2,4,7,10,13,17,19,22,26,28,31,36]
        unpleasant = [3,6,8,12,15,18,20,23,25,29,33,35]
        neutral = [1,5,9,11,14,16,21,24,27,30,32,34]


        video_index = []

        for i in range(1,self.event_size+1) :
            if i in neutral :
                video_index.append(0) # neutral index = 0
            elif i in pleasant : 
                video_index.append(1) # pleasant index = 1
            elif i in unpleasant :
                video_index.append(2) # unpleasant index = 2

        print("\n* Video Event by emotion *")
        print(video_index)
        self.video_index = video_index
        
    def load_data(self, file_name) :
        # file_name => 'data1.txt',
        user = pd.read_csv(file_name, sep = '	',header=None,names = ['HeartR', 'GSR', 'NAN'])
        user = user.drop('NAN', axis=1)
        user = user[self.sampling_rate * (self.user_time - self.event[0]):]
        
        user_hr = user['HeartR']
        user_gsr = user['GSR']
        
        self.user_hr = user_hr
        self.user_gsr = user_gsr
    
    # Apply dtrend and moving average filter to eliminate noise
    # gsr_window = 500, gsr_order = 5, hr_window = 800, hr_order = 5
    def filtering(self, gsr_window, gsr_order, hr_window, hr_order) :
        # GSR
        self.user_gsr = polynomial(self.user_gsr, order=gsr_order, plot=False) # Dtrend
        self.user_gsr = self.user_gsr.rolling(window=gsr_window).mean().dropna().values # Moving Average Filter
        plt.plot(self.user_gsr)
        plt.show()
        
        # Heart Rate
        self.user_hr = self.user_hr.rolling(window=hr_window).mean().dropna().values # Moving Average Filter    
        
        for i in range(20000, len(self.user_hr),20000) : # Apply dtrend filter every 20,000 data. 
            self.user_hr[i-20000:i] = polynomial(self.user_hr[i-20000:i], order=hr_order, plot=False) # Dtrend

            if(i+20000 > len(self.user_hr)) : 
                self.user_hr[i:] = polynomial(self.user_hr[i:], order=hr_order, plot=False) # Dtrend
                break;
        plt.plot(self.user_hr)
        plt.show()
    
    # Categorizing and processing by data subject #
    def categorizing(self) :
        # create list to store output for categorizing by subject      
        videoGSR = []
        videoGSR.append([]) # for plesant
        videoGSR.append([]) # for neutral
        videoGSR.append([]) # for unplesant
        
        videoHR = []
        videoHR.append([]) # for plesant
        videoHR.append([]) # for neutral
        videoHR.append([]) # for unplesant
        
        # GSR Categorize
        for events,index in zip(self.event, range(self.event_size)) :
            # GSR Categorize # 
            # [3 seconds before event ~ When the event occurred] compute average
            avg_GSR = np.mean(self.user_gsr[self.sampling_rate*(events-3):self.sampling_rate*events+1])

            # [When the event occurred ~ 6 seconds after event] - compute deviations using averaging
            videoGSR[self.video_index[index]].append((self.user_gsr[self.sampling_rate*events:self.sampling_rate*(events+6)+1] - avg_GSR).tolist())
            
            # HR Categorize #
            # [3 seconds before event ~ 6 seconds after event] 
            videoHR[self.video_index[index]].append((self.user_hr[self.sampling_rate*(events-3):self.sampling_rate*(events+6)+1]).tolist())   
        
        self.videoGSR = videoGSR
        self.videoHR = videoHR
        
        if self.user_order == 1 : # set max signal for use of adaptive threshold
            emotional_analysis.initial_user_max = np.max(self.user_gsr)
    
    # if not initial user, the user-entered values do not affect the analysis results.
    
    def find_adaptiveT(self) :
        # Max Sernsor Data for each user
        user1MS = emotional_analysis.initial_user_max
        user2MS = np.max(self.videoGSR)
        adaptiveT = user1MS / user2MS

        print("user1 max sensor data: ",user1MS)
        print("user2 max sensor data: ",user2MS)
        print("user1, user2 % : ", adaptiveT)
        
        return adaptiveT
    
    def analyze_gsr(self, threshold) :
        # create list to store analysis result
        gsr_result = []
        gsr_result.append([]) # for plesant
        gsr_result.append([]) # for neutral
        gsr_result.append([]) # for unplesant
        
        # If not initial user, applay adpative threhold & increase signal by adaptive threshold
        if self.user_order != 1 :
            threshold = self.find_adaptiveT()
            
            # Increase signal of user2 using adaptive threshold
            for i in range(len(self.videoGSR)) :
                for j in range(len(self.videoGSR[i])) :
                    for m in range (len(self.videoGSR[i][j])) :
                        self.videoGSR[i][j][m] = self.videoGSR[i][j][m] * threshold
            threshold = emotional_analysis.initialT / threshold
        else :
            emotional_analysis.initialT = threshold
        
        
        # Analyize
        for i in range(len(self.videoGSR)) :
            if i == 0 : print("**************** Neutral ****************\n")
            elif i == 1 : print("**************** Pleasant ****************\n")
            else : print("**************** Unpleasant ****************\n")
            for j in range(len(self.videoGSR[i])) :
                _peaks, _ = find_peaks(self.videoGSR[i][j], height=threshold)

                # Find Max Peak
                if len(_peaks) != 0 : # When there is more than one peak
                    peak_value = [] # list for peak's y value

                    for peak_index in _peaks : 
                        peak_value.append(self.videoGSR[i][j][peak_index]) # peak's y value

                    max_peak = np.max(peak_value)
                    max_peak_index = _peaks[peak_value.index(max_peak)]
                    print("max_peak :",max_peak)
                    print("max_peak_index :", max_peak_index)

                    # Exclude peak that do not affect to result for better accuracy
                    if max_peak_index > 9900 and max_peak_index < 12000 and (max_peak >= 0.036 and max_peak < 0.037) or ( max_peak < 0.35 and max_peak > 0.3) :
                        _peaks = [] 

                if(len(_peaks)>=1) : 
                    print("Emotion : pleasant or unpleasant")
                    gsr_result[i].append(1)
                else :
                    print("Emotion : neutral")
                    gsr_result[i].append(0)
                plt.ylim(-1,1)
                plt.axis(option='auto')
                plt.plot(self.videoGSR[i][j])
                plt.show()
                print("\n")
                
        self.gsr_result = gsr_result
        print("Neutral: ",gsr_result[0].count(0))
        print("Pleasant:", gsr_result[1].count(1))
        print("Unpleasant:", gsr_result[2].count(1))
   
    # threshold = 0.01 & distance = 500
    def analyze_hr(self,threshold_hr, distance) :
        # create list to store analysis result
        hr_result = []
        hr_result.append([]) # for plesant
        hr_result.append([]) # for neutral
        hr_result.append([]) # for unplesant

        for i in range(len(self.videoHR)) :
            if i == 0 : print("**************** Neutral ****************\n")
            elif i == 1 : print("**************** Pleasant ****************\n")
            else : print("**************** Unpleasant ****************\n")
            for j in range(len(self.videoHR[i])) :
                error = 0 # there is error -> 1 / not error -> 0

                # Peak detection for compute heart rate
                _peaks, _ = find_peaks(self.videoHR[i][j], height=threshold_hr, distance=distance)

                peak_time = []
                for t in range(1, len(_peaks)) :
                    peak_time.append(60 / ((_peaks[t] - _peaks[t-1])/2000))
                    # If the time between the two peaks is greater than 0.05, 
                    # It's highly likely that the peak wasn't found properly.
                    # Therefore, the threshold should be lowered. 
                    if peak_time[t-1] > 0.05 : error +=1 # if 
                        
                if error != 0 : # If there are problems -> set threahold to -0.03 & again peak detection
                    _peaks, _ = find_peaks(self.videoHR[i][j], height=-0.03, distance=distance)

                    # Compute heart rate using distance between peaks
                    peak_time = [60 / ((_peaks[t] - _peaks[t-1])/2000) for t in range(1, len(_peaks))]

                # Find Max Peak
                if len(_peaks) != 0 : # When there is more than one peak
                    peak_value = [] # list for peak's y value

                    for peak_index in _peaks :
                        peak_value.append(self.videoHR[i][j][peak_index]) # peak's y value

                    max_peak = np.max(peak_value) 
                    max_peak_index = _peaks[peak_value.index(max_peak)]
                    print("max_peak :",max_peak)
                    print("max_peak_index :", max_peak_index)      


                if(len(_peaks) >= 1) : # there is peak
                    plt.plot(_peaks,peak_value, "or")
                plt.plot(self.videoHR[i][j])
                plt.show() # original data

                self.videoHR[i][j] = [60 / ((_peaks[t] - _peaks[t-1])/2000) for t in range(1, len(_peaks))]

                peak_sec = [_peaks/2000 for _peaks in _peaks if _peaks/2000 <= 3]
                peak_befo = peak_sec.index(np.max(peak_sec)) # peak index before 3s
                peak_after = peak_befo + 1  # peak index after 3s

                # Compute average using heart rate of 3 seconds before event  
                avg_heart = np.mean(self.videoHR[i][j][:peak_befo])

                # Compute deviation of 6 seconds after event using heart rate average 
                self.videoHR[i][j] = self.videoHR[i][j] - avg_heart    

                plt.axis(option='auto')
                plt.plot(self.videoHR[i][j]) # calculated heart rate
                plt.show()
                print("\n")

    def gsr_analysis_result(self) :
        return self.gsr_result
    
    def initial_data(self) :
        emotional_analysis.initial_user_max = 0 
        emotional_analysis.total_user = 0 
        emotional_analysis.initialT = 0 

