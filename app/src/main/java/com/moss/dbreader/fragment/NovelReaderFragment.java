package com.moss.dbreader.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.moss.dbreader.BookCaseManager;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.ui.IReaderPageAdapterNotify;
import com.moss.dbreader.ui.ReaderPageAdapter;
import com.moss.dbreader.ui.ReaderPanel;
import com.moss.dbreader.ui.ReaderPanel.IReadPanelNotify;

import java.util.ArrayList;

import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

/**
 * Created by tangqif on 2017/10/9.
 */

public class NovelReaderFragment extends Fragment {

    private NovelEngineService.NovelEngine engine = null;
    private int engineID = -1;
    private int sessionID = 0;
    private DBReaderNovel novel;
    private ReaderPageAdapter adapter = null;
    private int tmpIndex = -1;

    private GestureDetector.OnDoubleTapListener doubleTapListener = new GestureDetector.OnDoubleTapListener(){

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            ReaderPanel rp = (ReaderPanel) getActivity().findViewById(R.id.reader_panel);
            rp.setVisibility(View.VISIBLE);
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    };

    private GestureDetector detector = new GestureDetector(NovelReaderFragment.this.getContext(), new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    });



    IReaderPageAdapterNotify readerPageAdapterNotify = new IReaderPageAdapterNotify() {
        @Override
        public void update(int index) {

            String chap = BookCaseManager.getChapterText(novel.name,index);
            if(chap.length() > 0){
                NovelReaderFragment.this.adapter.addText(index, chap);
                return;
            }

            if (NovelReaderFragment.this.engine == null) {
                NovelReaderFragment.this.tmpIndex = index;
            } else {
                NovelReaderFragment.this.engine.fetchChapter(NovelReaderFragment.this.novel.chapters.get(index), NovelReaderFragment.this.engineID, NovelReaderFragment.this.sessionID);
            }
        }
    };

    IFetchNovelEngineNotify fetchNovelEngineNotify = new IFetchNovelEngineNotify() {
        @Override
        public void OnSearchNovels(int nRet, int engineID, int sessionID, final ArrayList<DBReaderNovel> novels) {

        }

        @Override
        public void OnFetchNovel(int nRet, int sessionID, DBReaderNovel novel) {

        }

        @Override
        public void OnFetchChapter(int nRet, int sessionID, final int index, final String cont) {
            Log.i("Andrei", "index is " + index + " text arrived");
            if (NovelReaderFragment.this.sessionID != sessionID) {
                return;
            }
            if (nRet != NO_ERROR) {
                return;
            }
            BookCaseManager.saveChapterText(novel.name,index,cont);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NovelReaderFragment.this.adapter.addText(index, cont);
                }
            });

        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NovelEngineService.NovelEngineBinder binder = (NovelEngineService.NovelEngineBinder) iBinder;
            NovelReaderFragment.this.engine = binder.getNovelEngine();
            NovelReaderFragment.this.engine.addNotify(fetchNovelEngineNotify);
            if (tmpIndex != -1) {
                engine.fetchChapter(novel.chapters.get(tmpIndex), engineID, sessionID);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            NovelReaderFragment.this.engine.removeNotify(fetchNovelEngineNotify);
        }
    };


    public void setNovelInfo(DBReaderNovel novel, int engineID, int curPage) {
        this.novel = novel;
        this.engineID = engineID;
        initializeViewPager(curPage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_novelreader, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Intent intent = new Intent(getActivity(), NovelEngineService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.engine != null) {
            this.engine.cancel();
            getActivity().unbindService(this.serviceConnection);
            this.engine = null;
        }
    }

    private void initializeViewPager(int curPage) {
        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.reader_viewpager);
        ArrayList<View> views = new ArrayList<View>();
        detector.setOnDoubleTapListener(doubleTapListener);
        for (int i = 0; i < 4; i++) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.view_reader, null);
            views.add(v);
            v.setLongClickable(true);
            v.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return detector.onTouchEvent(event);
                }
            });
        }

        this.adapter = new ReaderPageAdapter(views, R.id.reader_text, R.id.reader_chapter_title, R.id.reader_chapter_page_no, 0, readerPageAdapterNotify);

        for (int i = 0; i < this.novel.chapters.size(); i++) {
            ReaderPageAdapter.ReaderPage rp = new ReaderPageAdapter.ReaderPage();
            rp.begin = ReaderPageAdapter.FLAG_PREVIOUS_PAGE;
            rp.chapterIndex = i;
            rp.name = this.novel.chapters.get(i).name;
            this.adapter.addPage(rp);
        }

        vp.addOnPageChangeListener(adapter);
        this.adapter.setCurrentItem(curPage);
        vp.setAdapter(adapter);
        vp.setCurrentItem(curPage);
    }

//    private void test(ReaderPageAdapter adapter){
//        String con1 = "凉风习习，夜色迷离，轻纱般的薄雾缭绕着安静的县城。\n" +
//                "　　朦胧月光映照着清清的小河，河水从拱桥下缓缓流淌，岸边是鳞次栉比的两三层黑瓦小楼。水渍斑驳的墙面上，尽是青绿色的苔藓痕迹，还有些爬满了常青藤蔓，只露出开在临河一面的一溜窗户。\n" +
//                "　　此时已是三更半夜，除了河中的蛙声，巷尾的犬吠，再也听不到半分声音，只有东头一个窄小的窗洞里，透出昏黄的灯光，还有说话声隐隐传来……\n" +
//                "　　从敞开的窗户往里看，仅见一桌一凳一床，桌上点一盏黑乎乎的油灯，勉强照亮着三尺之间。长凳上搁一个缺个口的粗瓷碗，碗里盛着八九个罗汉豆子。一个身着破旧长袍，须发散乱，望之四十来岁的男人蹲在边上，一边照料着身前的小泥炉，一边与对面床上躺着的十几岁少年说话。\n" +
//                "　　他说一口带着吴侬腔调的官话，声音嘶哑道：“潮生啊，你且坚持一些，待为父煎好药，你服过便可痊愈了也。”\n" +
//                "　　床上那少年心中轻叹一声，暗道：‘这该是第三十遍念叨了吧？’但知道是为自己着急，也就不苛责他了。微微侧过头去，少年看到那张陌生而亲切的脸上，满是汗水和急切，心中顿感温暖。知道一时半会他也忙不完，便缓缓闭上眼睛，回想着近日来发生的不可思议。\n" +
//                "　　他本是一名年轻的副处长，正处在人生得意的阶段，却在一觉醒来，附身在这个奄奄一息的少年身上。并在少年神魂微弱之际，莫名其妙地与之融合，获得了这少年的意识和记忆，成为了这个五百年前的少年。\n" +
//                "　　是庄周还是蝴蝶？是原来的我还是现在的沈默？他已经完全糊涂了，似乎既是又是，似乎既不是也不是，或者说已经是一个全新的沈默了吧。\n" +
//                "　　事情就是这样荒诞，然而却确实发生，让他好几天无法面对，但后来转念一想，反正自己是个未婚的孤儿，无牵无挂，在哪里不是讨生活？再说用原先的副处级，换了这年青十好几岁的身体，似乎还是赚到了。\n" +
//                "　　只是突然生出许多属于那少年的情感，这让他有些不适应。\n" +
//                "　　适者生存，所以一定要适应。沈默这样对自己说道。\n" +
//                "　　一旦放开心怀，接受了新身份，一些属于那少年的记忆便潮水般涌来。他知道自己叫沈默，乳名唤作潮生，十三岁。是大明朝绍兴府会稽县永昌坊沈贺的独子。\n" +
//                "　　要说这沈贺，出身绍兴大族沈家的旁支，家境尚算小康，自幼在族学中开蒙，学问那是很好的。十八岁便接连考中县试、府试、院试，成为一名每月领取廪米的廪生——廪生就是秀才，但秀才却不一定是廪生，因为只有考取一等的寥寥数人能得到国家奉养。\n" +
//                "　　能靠上这吃皇粮的秀才，沈贺很是给爹娘挣了脸面。\n" +
//                "　　然而时运倒转、造化弄人，沈相公从十九岁第一次参加秋闱开始，接连四次落第，这是很正常的事情，因为江浙一带乃是人文荟萃之地，绍兴府又拔尽江南文脉。余姚、会稽、山阴等几个县几乎家家小儿读书，可谓是藏龙卧虎，每年都有大批极优秀的读书人应举。\n" +
//                "　　名额有限、竞争残酷。像沈相公这样的，在别处早就中举了，可在绍兴这地方，却只能年复一年成为别人的陪衬。后来父母相继过世，他又连着守孝五年，等重新出来考试的时候，已经三十好几，应试最好的年纪也就过去了……\n" +
//                "　　可沈秀才这辈子就读书去了，不考试又能作甚？他不甘心失败，便又考了两届，结果不言而喻，空把的大好光阴都不说，还把颇为殷实的家底败了个干干净净，日子过的极为艰难，经年吃糠咽菜，见不到一点荤腥。\n" +
//                "　　去年夏天，沈秀才的媳妇中了暑气，积弱的身子骨竟一下子垮了。为了给媳妇看病，他连原来住的三进深的宅子都典卖了。结果人家欺他用急，将个价值百两的宅子，硬生生压到四十两，沈秀才书生气重，不齿于周借亲朋，竟真的咬牙卖掉了房产，在偏远巷里赁一栋廉价小楼，将老婆   孩子安顿住下，给媳妇延医问药。";
//
//        String con2 = "沈默不能入眠，他借着幽暗的天光，端详着趴在桌子上的父亲，心中久久无法平静。\n" +
//                "　　他不是为眼前的衣食发愁，虽然这看起来是个大问题，但有这位父亲在，应该不会让自己活活饿死吧。\n" +
//                "　　他更不是为将来的命运发愁，他相信只要自己恢复健康，命运就一定在自己手中。不管身处何时何地，他相信自己一定行。\n" +
//                "　　他睡不着觉的原因，说出来要笑掉一些人的大牙——他为能有一个关爱自己的父亲而兴奋不已。也许是性格的融合，也许是心底的渴望，他对这个一看就是人生失败者的父亲，除了称呼起来难以为情之外，竟然一点都不排斥。\n" +
//                "　　前世的孤独和无助深刻的告诉他，努力奋斗可以换来成功和地位，金钱和美女，却惟独换不来父母亲情。那是世上最无私、最纯粹、最宝贵的东西啊，可他偏生就从来不曾拥有。\n" +
//                "　　现在上天给他一个拥有的机会，这对于一个自幼便是孤儿，从未享受过天伦之乐的人来说，简直是最珍贵的礼物！\n" +
//                "　　所以沈默决定放开心怀，努力地去接受他，去享受这份感情……\n" +
//                "　　一夜在胡思乱想中度过，不知不觉天就亮了，小鸟在窗台上叽叽喳喳的觅食，也把趴在桌上的沈贺叫醒了。他揉揉眼睛，便往床上看去，只见沈默正在微笑地望着自己。\n" +
//                "　　沈贺的眼泪一下子就夺眶而出，起身往床边跑去，却被椅腿绊一下，踉跄几步，险些一头磕在床沿上。他却不管这些，一把抓住沈默的手，带着哭腔道：“天可怜见，佛祖菩萨城隍爷保佑，终于把我儿还我了……”\n" +
//                "　　沈默用尽全身力气，反握一下他的手，嘶声道：“莫哭……”虽然已经接受了，但‘爹爹’二字岂是那么容易脱口？\n" +
//                "　　沈贺沉浸在狂喜之中，怎会注意这些枝节末梢，抱着他哭一阵笑一阵，把个大病未愈的潮生儿弄得浑身难受，他却一味忍着，任由沈贺发泄心情。\n" +
//                "　　过一会儿，沈贺可能觉着有些丢脸，便擦着泪红着眼道：“都是爹爹不好，往日里沉迷科场，不能自拔，结果把个好好的家业败了精光，还把你娘拖累死了……”一想到亡妻，他的泪水又盈满眼眶，哽咽道：“你娘临去的时候，千叮咛，万嘱咐，让我一定把你拉扯成人。可她前脚走，我就险些  把你给没了……我，我沈贺空读圣贤之书，却上不孝于父母，中有愧于发妻，下无颜于独子，我还有何面孔能立于世啊……”\n" +
//                "　　沈默前世成精，揣测人心的能力，并没有随着身份的转换而消失，他能感到沈贺正处在‘自我怀疑自我反省’的痛苦阶段，要么破而后立，要么就此沉沦了。\n" +
//                "　　他本想开导几句，给老头讲一讲‘三百六十行、行行出状元’、‘只有笨死的狗熊，没有憋死的活人’之类的人生道理。但转念一想，自己个当儿子的，说这些话显然不合适，便无奈住了嘴。\n" +
//                "　　不过沈默觉着有自己在，老头应该会重回新振作起来，便紧紧握着他的手，无声的给他力量。";
//
//        String con3 = "草草吃过早饭，沈贺先将家什一收拾，再把个瓦盆端到床下，嘱咐道：“想解手就往这里面，爹爹出去转转。”便急匆匆掩门下楼，逃也似的去了。\n" +
//                "　　他一走，小小的阁楼内便安静下来，外面的喧闹声却渐渐传了进来。\n" +
//                "　　透过虚掩的窗户，沈默看到蓝莹莹的天空上飘着洁白的云，颜色是那么的纯粹。这个见惯了灰蒙蒙天空的小子不由痴了，好长时间才回过神来，支起耳朵听窗外的动静，他听见有船儿过水的辘辘声，有吴侬软语的调笑声，还有些孩童戏耍的欢笑声。\n" +
//                "　　躺了一会，还是睡不着。沈默使劲撑起胳膊，想要坐住身子往外看看，无奈身体仿若灌了铅，重又摔回在硬床板上，痛得他嘶嘶直抽冷气。\n" +
//                "　　他偏生是个犟种，越是起不来越是反复尝试。不一会儿，便折腾得满身虚汗，直挺挺躺在床上，呼哧呼哧的喘着粗气。\n" +
//                "　　这时房门被粗暴的推开，起先那胖女人又出现在沈默面前，还有个身材干瘦的汉子，背着个大箱子，低头跟在她后面。\n" +
//                "　　那女人早就看到沈贺离开，大模大样地走进来，一屁股坐在长凳上，看也不看沈默一眼，对那汉子指指点点道：“搁到角上去，再把那些个箩筐也拿上来。”\n" +
//                "　　那汉子看看满头大汗的沈默，于心不忍道：“这小哥病着呢，我们还是莫打扰了。”\n" +
//                "　　“让个小娘养的死去。”胖女人轻蔑地看沈默一眼，怒冲冲道：“我们家都插不下脚了，不搁这里搁哪处？”\n" +
//                "　　“可以放在底楼嘛。”汉子小心翼翼道。\n" +
//                "　　“放个屁啊。”胖女人怒道：“苦霪雨，水漉漉，我的家什长蘑菇怎办？你个穷鬼再给我买新的啊？”说着矛头又转移到汉子身上，指着鼻子骂他穷光光、没出息，跟了他算倒八辈子大霉，不去偷汉子就是他祖上冒青烟之类。\n" +
//                "　　沈默在边上默默听着，暗道：‘倘若真有人和你偷情，那才是你祖坟上冒青烟了呢。’\n" +
//                "　　那汉子被婆娘骂得窘迫不已，赶紧将箱子往地上一搁，丢下一句：“俺再下去取。”便落荒而逃了。\n" +
//                "　　那胖女人朝着他的背影狠啐一声，又觉着意犹未尽，准备再寻沈默的晦气耍耍。\n" +
//                "　　沈默却剧烈的咳嗽起来，脸蛋憋得一阵白一阵红。再配上那满头的大汗，一看就是重病在身的样子。\n" +
//                "　　见他不停咳嗽，那女人试探问道：“侬素啥西病？”\n" +
//                "　　沈默喘息道：“老……”便又接着咳嗽起来。\n" +
//                "　　“啥西？痨……痨病？”胖女人面色顿时煞白，如坐了钉子一般，一蹦三尺高。尖叫一声，便连滚带爬的夺门而出。出门时没留神，被门槛一绊，一下子摔了出去，正好撞在一手拎个包袱往上上的汉子怀里，两人便如皮球一般，骨碌碌地滚了下来。\n" +
//                "　　沈默只听到一阵稀里轰隆的声响，紧接着便是那女人杀猪般的嚎叫声：“你不会接住我啊……”\n" +
//                "　　“俺接不住啊……”汉子委屈巴巴的声音从楼下传来。";
//
//        adapter.addText(0,con1);
//        adapter.addText(1,con2);
//        adapter.addText(2,con3);
//        for(int i = 0 ; i < 3 ; i++){
//            ReaderPageAdapter.ReaderPage rp = new ReaderPageAdapter.ReaderPage();
//            rp.chapterIndex = i;
//            rp.begin = -2;
//            rp.name = "第" + i + "章";
//            adapter.addPage(rp);
//        }
//
//    }

}
