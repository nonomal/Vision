# SW-Vision

#### 介绍
随着科技的高速发展，手机的相机性能也日益加强，越来越多的人选择用手机代替繁琐复杂的专业设备进行摄影。
但是，涉及到多设备的手机摄影也存在很多可以改善的地方。在合影场景下，难以实现便捷方便的预览画面与控制拍摄。当想要同时多角度抓拍或者多机位录制时，需要同时操作多台移动设备，操作难度大，无法直接生成融合效果，还需要后期合成。
因此，我们以强化端到端数据传输，减轻云服务器流量为前提，利用多设备互联，集成多设备采集优势，实现远程合拍、多角度捕捉、多机位录制，及高清晰度的一体融合效果，产生本项目。
我们的项目已经实现了以下功能：
（1）多设备互联：多设备在同一局域网下进行连接，可选择作为采集端或控制端，我们考虑的多设备包含控制端手机、采集端手机、手表、云台等。
（2）跨设备操作：采集端负责提供摄像头画面给控制端，同时也可以控制自己的设备；控制端不仅可以查看采集端画面，也能对采集端进行参数调整、拍摄录制等操作；
（3）一键同时操作多设备：控制端可以一键操控多设备开始同时拍摄或录制，来对一个场景不同角度的画面进行捕捉。
（4）同屏实时显示多设备画面：用户可以选择查看一个或多个想要观察的画面；
我们的项目着重考虑了以下场景的相关应用：
（1）合影场景：设计了远程预览和远程控制拍摄，便于调整后进行合影，可以应用到手表端。
（2）多角度捕捉：可用于多设备对一个拍摄对象进行多角度拍摄、对运动时某一个瞬间的动作情况进行捕捉、得到一个场景的全视角融合结果
（3）多机位录制：可用做举办小型活动时的小型导播台，实现实时查看由各终端组成的多机位情况、随时切换多设备的角度记录活动现场画面，可以使用集成人脸识别模块的云台操作。
与已有相关方案相比，我们有如下创新点：
（1）目前已有方案均采用以云服务器为主要数据传输载体，本项目实时和无损数据传输均主要通过端到端进行，大幅度减轻云服务器压力
（2）本项目利用多设备采集优势，实现远程合拍、多角度捕捉、多机位录制，并实现相关高清晰度的一体融合效果
（3）本项目实现对多设备相机资源的实时预览、控制，弥补市场已有抓拍方案的单一性、固定性，同时采集端仍然有自主控制权
（4）智能化手机端人脸识别控制云台跟随
#### 软件架构
软件架构说明


#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
