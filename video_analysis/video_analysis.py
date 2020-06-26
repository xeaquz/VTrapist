


import numpy as np
import cv2
from pytube import YouTube
from PIL import Image
# Using Youtubelibrary so download the video
#YouTube('https://www.youtube.com/watch?v=9bZkp7q19f0').streams.first().download()
def Detect_Corner(old_gray,**feature_params):
    p0 = cv2.goodFeaturesToTrack(old_gray, mask = None, **feature_params)
    return p0


def Cal_OpticalFlow(old_gray,frame_gray,p0,**lk_params):
    
    #Cal_OpticalFlow(old_gray,frame_gray,p0,**lk_params)
    p1, st, err = cv2.calcOpticalFlowPyrLK(old_gray, frame_gray, p0, None, **lk_params)
    return p1,st,err
    
    
def read_video(root_video):
    
    f=open('timestamp_result.txt',mode='w',encoding='utf-8')
    cap = cv2.VideoCapture(root_video)


    fps=cap.get(cv2.CAP_PROP_FPS)
    timestamps=[cap.get(cv2.CAP_PROP_POS_MSEC)]
    calc_timestamps=[0,0]

    # params for ShiTomasi corner detection
    feature_params = dict( maxCorners = 100,
                           qualityLevel = 0.01,
                           minDistance = 30,
                           blockSize = 14)
 
    # Parameters for lucas kanade optical flow
    lk_params = dict( winSize  = (15,15),
                      maxLevel = 0,
                      criteria = (cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_COUNT, 10, 0.03))
 
    # Create some random colors
    color = np.random.randint(0,255,(100,3))
 
    # Take first frame and find corners in it
    ret, old_frame = cap.read()
    old_gray = cv2.cvtColor(old_frame, cv2.COLOR_BGR2GRAY)
    
    
    p0=Detect_Corner(old_gray,**feature_params)
 
    # Create a mask image for drawing purposes
    mask = np.zeros_like(old_frame)
    count=0 
    check=0
    ## to save the color infor in frame 
    prev_r=0
    prev_g=0
    prev_b=0
    ## to compare the previous frame.
    new_r=0
    new_g=0
    new_b=0


    while(1):
        ret,frame = cap.read()
        # if ret value null then stop the read video.
        if not ret:
            break
        # detect the corner, code convert the image to gray scale
        frame_gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        img_color=cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        ##get the RGB inforamtion from this frame, select the 200,200 pixels RGB value.
        ## save the each value in b g r 
        b=frame.item(200,200,0)
        g=frame.item(200,200,1)
        r=frame.item(200,200,2)
    
        #to compare the previous frame save the each RGB value
        new_r=r
        new_g=g
        new_b=b
    
        ## first frame can't select previous frame
        if check==0:
            new_r=r
            new_g=g
            new_b=b
            check=check+1
        # If not first frame then get sum of previous frame and present frame.
        # and get the difference each sum of frame.
        else:
            sum_color=(new_r-prev_r)+(new_r-prev_r)+(new_r-prev_r)
            if sum_color<0:
                sum_color=sum_color*(-1)
        
            # check the sum of RGB in 200,200 pixel each frame bigger than 100 then we select this frame has keyframe 
            if sum_color>100:
                mask=0
                frame=0
                ##search in this frame time and append it in timestamps array/
                timestamps.append(cap.get(cv2.CAP_PROP_POS_MSEC))
                calc_timestamps.append(calc_timestamps[-1]+1000/fps)
                p0 = cv2.goodFeaturesToTrack(old_gray, mask = None, **feature_params)
                # restart find feature.
            
        
    
        # calculate optical flow
        
        p1, st, err=Cal_OpticalFlow(old_gray,frame_gray,p0,**lk_params)
        
        

 
        # Select good points
        good_new = p1[st==1]
        good_old = p0[st==1]
        # to compare next frame present pixel color in frame assign to previous RGB value
        prev_r=new_r
        prev_g=new_g
        prev_b=new_b
    
 
        # draw the tracks
        for i,(new,old) in enumerate(zip(good_new,good_old)):
            a,b = new.ravel()
            c,d = old.ravel()
        
            mask = cv2.line(mask, (a,b),(c,d), color[i].tolist(), 2)
            frame = cv2.circle(frame,(a,b),5,color[i].tolist(),-1)

        img=cv2.add(frame,mask)
        # show the corner and optical flow
 
        cv2.imshow('frame',img)
        k = cv2.waitKey(30) & 0xff
       
    
        if k == 27:
            break
        elif k==ord('s'):
            cv2.imwrite('opticalresult %d.png' %count,frame)
            count=count+1
    
 
        # Now update the previous frame and previous points
        old_gray = frame_gray.copy()
        p0 = good_new.reshape(-1,1,2)
        #print(p0[[0]])
    
    
   
 
    cv2.destroyAllWindows()

    print(timestamps)
    #save the information of itmestamps in txt file.
    for i, (ts, cts) in enumerate(zip(timestamps,calc_timestamps)):
        print('FRAME : %d'%i,abs(ts-cts))
        f.write(str(abs(ts-cts))+'\n')
    f.close()









print('input your video root : ')
root_video=input()

print(root_video)

read_video(root_video)

# Open the file to save the timestamps 




# In[ ]:



