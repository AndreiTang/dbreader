package com.moss.dbreader;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.moss.dbreader.ui.ReaderPageAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<View> views = new ArrayList<View>();
        for(int i = 0 ; i < 4 ; i++){
            View v = getLayoutInflater().inflate(R.layout.view_reader,null);
            views.add(v);
        }

        ReaderPageAdapter.ReaderPage rp = new ReaderPageAdapter.ReaderPage();
        rp.begin = rp.end = -1;
        rp.chapterIndex = 0;
        String tt =  "第一章 一梦五百年 （上）\n\n";

        tt += "凉风习习，夜色迷离，轻纱般的薄雾缭绕着安静的县城。\n" +
                "\n" +
                "朦胧月光映照着清清的小河，河水从拱桥下缓缓流淌，岸边是鳞次栉比的两三层黑瓦小楼。水渍斑驳的墙面上，尽是青绿色的苔藓痕迹，还有些爬满了常青藤蔓，只露出开在临河一面的一溜窗户。\n" +
                "\n" +
                "此时已是三更半夜，除了河中的蛙声，巷尾的犬吠，再也听不到半分声音，只有东头一个窄小的窗洞里，透出昏黄的灯光，还有说话声隐隐传来……\n" +
                "\n" +
                "从敞开的窗户往里看，仅见一桌一凳一床，桌上点一盏黑乎乎的油灯，勉强照亮着三尺之间。长凳上搁一个缺个口的粗瓷碗，碗里盛着八九个罗汉豆子。一个身着长衫，须发散乱，望之四十来岁的男人蹲在边上，一边照料着身前的小泥炉，一边与对面床上躺着的十几岁少年说话。\n" +
                "\n" +
                "他说一口带着吴侬腔调的官话，声音嘶哑道：“潮生啊，你且坚持一些，待为父煎好药，你服过便可痊愈了也。”\n" +
                "\n" +
                "床上那少年心中轻叹一声，暗道：‘这该是第三十遍念叨了吧？’但知道是为自己着急，也就不苛责他了。微微侧过头去，少年看到那张陌生而亲切的脸上，满是汗水和急切，心中顿感温暖。知道一时半会他也忙不完，便缓缓闭上眼睛，回想着近日来发生的不可思议。\n" +
                "\n" +
                "他本是一名年轻的副处长，正处在人生得意的阶段，却在一觉醒来，附身在这个奄奄一息的少年身上。并在少年神魂微弱之际，莫名其妙的与之融合，获得了这少年的意识和记忆，成为了这个五百年前的少年。\n" +
                "\n" +
                "是庄周还是蝴蝶？是原来的我还是现在的沈默？他已经完全糊涂了，似乎即是又是，似乎既不是也不是，或者说已经是一个全新的沈默了吧。\n" +
                "\n" +
                "事情就是这样荒诞，然而却确实发生，让他好几天无法面对，但后来转念一想，反正自己是个未婚的孤儿，无牵无挂，在哪里不是讨生活？再说用原先的副处级，换了这年青十好几岁的身体，似乎还是赚到了。\n" +
                "\n" +
                "只是突然生出许多属于那少年的情感，这让他有些不适应。\n" +
                "\n" +
                "适者生存，所以一定要适应。沈默这样对自己说道。\n" +
                "\n" +
                "一旦放开心怀，接受了新身份，一些属于那少年的记忆便潮水般涌来。他知道自己叫沈默，乳名唤作潮生，十三岁。是大明朝绍兴府会稽县永昌坊沈贺的独子。\n" +
                "\n" +
                "要说这沈贺，出身绍兴大族沈家……的旁支，家境尚算小康，自幼在族学中开蒙，学问那是很好的。十八岁便接连考中县试、府试、院试，成为一名每月领取廪米的廪生……廪生就是秀才，但秀才却不一定是廪生，因为只有考取一等的寥寥数人能得到国家奉养。\n" +
                "\n" +
                "能靠上这吃皇粮的秀才，沈贺很是给爹娘挣了脸面。\n" +
                "\n" +
                "然而时运倒转、造化弄人，沈相公从十九岁第一次参加秋闱开始，接连四次落第，这是很正常的事情，因为江浙一带乃是人文荟萃之地，绍兴府又拔尽江南文脉。余姚、会稽、山阴等几个县几乎家家小儿读书，可谓是藏龙卧虎，每年都有大批极优秀的读书人应举。\n" +
                "\n" +
                "名额有限、竞争残酷。像沈相公这样的，在别处早就中举了，可在绍兴这地方，却只能年复一年成为别人的陪衬。后来父母相继过世，他又连着守孝五年，等重新出来考试的时候，已经三十好几，应试最好的年纪也就过去了……\n" +
                "\n" +
                "可沈秀才这辈子就读书去了，不考试又能作甚？他不甘心失败，便又考了两届，结果不言而喻……空把的大好光阴都不说，还把颇为殷实的家底败了个干干净净，日子过的极为艰难，经年吃糠咽菜，见不到一点荤腥。\n" +
                "\n" +
                "去年夏天，沈秀才的媳妇中了暑气，积弱的身子骨竟一下子垮了。为了给媳妇看病，他连原来住的三进深的宅子都典卖了。结果人家欺他用急，将个价值百两的宅子，硬生生压到四十两，沈秀才书生气重，不齿于周借亲朋，竟真的咬牙卖掉了房产，在偏远巷里赁一栋廉价小楼，将老婆孩子安顿住下，给媳妇延医问药。\n" +
                "\n" +
                "结果银钱流水般的花出去，沈默他妈的病却越来越重，到秋里卧床不起，至年前终于阖然而逝。沈贺用剩下的钱葬了妻子，却发现连最便宜的小楼都租不起了，爷俩只好‘结庐而居’。\n" +
                "\n" +
                "当然这是沈相公的斯文说法，实际上就是以竹木为屋架，以草苫覆盖遮拦，搭了个一间到底的草舍。虽然狭窄潮湿，但总算有个窝了不是？\n" +
                "\n" +
                "这时一家人唯一的收入来源，便是县学发的廪米，每月六斗。按说省着点，勉强也能凑合，但‘半大小子，饿死老子’，沈默正是长身体的时候，食量比他爹还大，这点粳米哪能足够？沈秀才只得去粮铺换成最差的籼米，这样可以得到九斗。沈默再去乡间挖些野菜、捉些泥鳅回来，这才能刚刚对付两人的膳食。\n" +
                "\n" +
                "俗话说祸不单行，一点也不假，几天前沈默去山上挖野菜，竟然被条受惊的毒蛇给咬了小腿，被同去的哥儿几个送回来时，已经是满脸黑气，眼看就要不行了。\n" +
                "\n" +
                "后来发生的事情，沈默就不知道了。当他悠悠醒来，便发现自己已经置身于一间阁楼之中。虽然檩柱屋顶间挂满了蜘蛛落网，空气中还弥散着一股腐朽酸臭的味道，却比那透风漏雨、阴暗潮湿的草棚子要强很多。\n" +
                "\n" +
                "正望着一只努力吐丝的蜘蛛出神，沈默听……父亲道：“好了好了，潮生吃药了。”便被扶了起来。他上身靠在枕头上，端量着今后称之为父的男人，只见他须发蓬乱，脸色青白，眼角已经有了皱纹，嘴角似乎有些青淤，颧骨上亦有些新鲜的伤痕。身上的长衫也是又脏又破，仿佛跟人衅过架，还不出意料输了的样子。\n" +
                "\n" +
                "见沈默睁眼看自己，沈贺的双目中满是兴奋和喜悦，激动道：“得好生谢谢殷家小姐，若没得她出手相救，咱爷俩就得阴阳永隔了……”说着便眼圈一红，啪嗒啪嗒掉下泪来。\n" +
                "\n" +
                "看到他哭，沈默的鼻头也有些发酸，想要开口安慰一下，喉咙却仿佛加了塞子一般，一个字也说不出来。\n" +
                "\n" +
                "注意到他表情的变化，沈贺赶紧擦擦泪道：“怎么了，你哪里不舒服吗？”见沈默看向药碗，沈贺不好意思道：“险些忘记了。”便端起碗来，舀一勺褐色的汤药，先在嘴边吹几下，再小心的搁到他嘴边。\n" +
                "\n" +
                "沈默皱着眉头轻啜一口，却没有想象中那么苦涩，反倒有些苦中带甜。见他眉头舒缓下来，沈贺高兴道：“你从小不爱吃药，我买了些杏花蜜掺进去，大夫说有助于你复原的。”便伺候着他将一碗药喝下去。\n" +
                "\n" +
                "用毛巾给沈默擦擦嘴，再把他重新放躺，沈贺很有成就感的长舒口气，仿佛做完一件大事一般。这才直起身，将空药碗和破碗搁到桌上，一屁股坐在凳子上，疲惫的弯下腰，重重喘一口粗气。\n" +
                "\n" +
                "沈默见他盛满一碗开水，从破碗中捻起三粒青黄色的蚕豆，稍一犹豫，又将手一抖，将其中两粒落回碗中，仅余下一颗捏在手中。\n" +
                "\n" +
                "端详那一粒豆子许久，沈贺闭上眼，将其缓缓送入口中，慢慢咀嚼起来，动作极是轻柔，仿佛在回味无穷，久久不能自拔。\n" +
                "\n" +
                "良久，沈贺才缓缓睁开眼，微微摇头赋诗道：“曹娥运来芽青豆，谦裕同兴好酱油；东关请来好煮手，吃到嘴里糯柔柔。”\n" +
                "\n" +
                "沈默汗颜，他从来不知道，原来吃一个豆也会引起这么大的幸福感。\n" +
                "\n" +
                "见他流露出不以为然的神情，沈贺轻抿一口开水道：“潮生，你是没有尝到啊，这\n" +
                "\n" +
                "豆肉熟而不腐、软而不烂，咀嚼起来满口生津，五香馥郁，又咸而透鲜，回味微甘……若能以黄酒佐之，怕是土地公公都要来尝一尝的。”\n" +
                "\n" +
                "‘土地公就没吃过点好东西？’沈默翻翻白眼，却被沈贺以为在抱怨他吃独食，连忙解释道：“不是为父不与你分享，而是大夫嘱咐过，你不能食用冷热酸硬的东西，还是等痊愈了再说吧。”\n" +
                "\n" +
                "沈默无力的点点头，见沈贺又用同样的速度吃掉两颗，便将手指在抹布上揩了楷，把一碗水都喝下去，一脸满足道：“晚饭用过，咱爷俩该睡觉了。”\n" +
                "\n" +
                "沈默的眼睛瞪得溜圆，沈贺一本正经道：“圣人云：‘事不过三’，这第一次吃叫品尝，第二次叫享受，第三次叫充饥，再多吃就是饕餮浪费了。”说着朝他挤眼笑笑道：“睡吧。”便吹熄油灯，趴在桌子上睡了。\n" +
                "\n" +
                "因为这屋里只有一张单人床……";
        tt=tt.replace("\n\n","\n");
        ReaderPageAdapter adp = new ReaderPageAdapter(views,R.id.reader_text,0);
        adp.addPage(rp);
        adp.addText(0,tt);

        tt = "第二章 一梦五百年 （中）\n\n";
        tt += "    沈默不能入眠，他借着幽暗的天光，端详着趴在桌子上的…父亲，心中久久无法平静。\n" +
                "\n" +
                "    他不是为眼前的衣食发愁，虽然这看起来是个大问题，但有这位…父亲在，应该不会让自己活活饿死……吧。\n" +
                "\n" +
                "    他更不是为将来的命运发愁，他相信只要自己恢复健康，命运就一定在自己手中。不管身处何时何地，他相信自己一定行。\n" +
                "\n" +
                "    他睡不着觉的原因，说出来要笑掉一些人的大牙——他为能有一个关爱自己的父亲而兴奋不已。也许是性格的融合，也许是心底的渴望，他对这个一看就是人生失败者的父亲，除了称呼起来难以为情之外，竟然一点都不排斥。\n" +
                "\n" +
                "    前世的孤独和无助深刻的告诉他，努力奋斗可以换来成功和地位，金钱和美女，却惟独换不来父母亲情。那是世上最无私、最纯粹、最宝贵的东西啊，可他偏生就从来不曾拥有。\n" +
                "\n" +
                "    现在上天给他一个拥有的机会，这对于一个自幼便是孤儿，从未享受过天伦之乐的人来说，简直是最珍贵的礼物！\n" +
                "\n" +
                "    所以沈默决定放开心怀，努力的去接受他，去享受这份感情……\n" +
                "\n" +
                "    一夜在胡思乱想中度过，不知不觉天就亮了，小鸟在窗台上叽叽喳喳的觅食，也把趴在桌上的沈贺叫醒了。他揉揉眼睛，便往床上看去，只见沈默正在微笑的望着自己。\n" +
                "\n" +
                "    沈贺的眼泪一下子就夺眶而出，起身往床边跑去，却被椅腿绊一下，踉跄几步，险些一头磕在床沿上。他却不管这些，一把抓住沈默的手，带着哭腔道：“天可怜见，佛祖菩萨城隍爷保佑，终于把我儿还我了……”\n" +
                "\n" +
                "    沈默用尽全身力气，反握一下他的手，嘶声道：“…莫哭……”虽然已经接受了，但‘爹爹’二字岂是那么容易脱口？\n" +
                "\n" +
                "    沈贺沉浸在狂喜之中，怎会注意这些枝节末梢，抱着他哭一阵笑一阵，把个大病未愈的潮生儿弄得浑身难受，他却一味忍着，任由沈贺发泄心情。\n" +
                "\n" +
                "    过一会儿，沈贺可能觉着有些丢脸，便擦着泪红着眼道：“都是爹爹不好，往日里沉迷科场，不能自拔，结果把个好好的家业败了精光，还把你娘拖累死了……”一想到亡妻，他的泪水又盈满眼眶，哽咽道：“你娘临去的时候，千叮咛，万嘱咐，让我一定把你拉扯成人。可她前脚走，我就险些把你给没了……我，我沈贺空读圣贤之书，却上不孝于父母，中有愧于发妻，下无颜于独子，我还有何面孔能立于世啊……”\n" +
                "\n" +
                "    沈默前世成精，揣测人心的能力，并没有随着身份的转换而消失，他能感到沈贺正处在‘自我怀疑自我反省’的痛苦阶段，要么破而后立，要么就此沉沦了。\n" +
                "\n" +
                "    他本想开导几句，给老头讲一讲‘三百六十行、行行出状元’、‘只有笨死的狗熊，没有憋死的活人’之类的人生道理。但转念一想，自己个当儿子的，说这些话显然不合适，便无奈住了嘴。\n" +
                "\n" +
                "    不过沈默觉着有自己在，老头应该会重回新振作起来，便紧紧握着他的手，无声的给他力量。\n" +
                "\n" +
                "    好半晌，沈贺的情绪才稳定下来，他擦干脸上的泪水，自嘲的笑笑道：“这辈子还没哭这么痛快呢。”轻拍一下沈默的肩膀，他面色极为复杂道：“苦读诗书数十载，方知世上无用是书生。从今天开始，我要找份营生，好好养活你！”\n" +
                "\n" +
                "    沈默感激的笑笑，想了想，还是开口道：“您不必勉强自己，等孩儿身体好些，自有计较，咱们无需为生计发愁。”说着呲牙笑笑道：“说不定下次就能高中呢。”\n" +
                "\n" +
                "    沈贺仿佛从不认识一般，上下打量着沈默，宠溺的揉揉他的脑袋，开心笑道：“天可怜见，潮生这次因祸得福，长大懂事了。”\n" +
                "\n" +
                "    沈默微微侧头，躲开沈贺的手，舔一下干裂的嘴唇道：“奋斗了半辈子的事情，放弃了岂不可惜？”\n" +
                "\n" +
                "    沈贺又是吃了一惊……这倒不怪他爱吃惊。一个以前还木讷难言的少年，突然说出这样深沉的话来，搁你身上你也吃。但沈相公毕竟是秀才出身，很快便联系到‘否极泰来’这样的玄学观点上，起身在屋里走几圈，兴奋的搓手道：“看来祖宗有灵，让我儿的灵窍早开，果真是冥冥中自有定数啊！”\n" +
                "\n" +
                "    沈默虽然不敢苟同，但对无需自我辩解很是满意，便紧抿着嘴，笑而不言。\n" +
                "\n" +
                "    沈贺又在屋里脚步沉重的转几圈，突然定住身形，十分严肃的望着沈默，仿佛做出了最重大的决断，沉声道：“潮生，为父决定了，就此不再读书了。”\n" +
                "\n" +
                "    沈默翻翻白眼，心道：‘感情我白说了。’便要开口劝道，却被沈贺挥手阻止道：“你好生将养身体，万事都不要操心，一切有爹爹呢。”\n" +
                "\n" +
                "    沈默隐约猜到他的决定，面露不忍道：“您……”话说到一般，却又被重重的敲门声打断。\n" +
                "\n" +
                "    爷俩回头望时，那门已经被推开，一个怒气冲冲的婆娘出现在两人眼前。只见她穿一身花花绿绿、皱皱巴巴的长裙，身材肥短、面目可憎。伸着根萝卜似的指头，指着他俩便开了骂：“侬个促老头和个小娘生，大清早上就在个堂里走来走去，着急起去报头胎啊！”\n" +
                "\n" +
                "    沈默对她的安昌土音很不适应……反正横竖是骂人的话，也没必要听下去。想将那臭婆娘撵出去，身上却没有半分力气，压根坐不起来；想要跟那女人拌嘴，又几乎听不懂她在说什么，只好闷闷的斜着眼，让老头对付她。\n" +
                "\n" +
                "    但沈贺显然不是这泼妇的对手，涨红了脸也说不出话来。被骂得狠了，才憋出一句道：“还不让人在自个屋里走道了么？”\n" +
                "\n" +
                "    “啥西？自个屋里头？”泼妇激动的唾沫横飞道：“这是侬家么？昨夜头还是我家阁楼好不好？”后面又是一阵语速极快的漫骂，沈默是一句也没听明白。\n" +
                "\n" +
                "    沈贺却听得明明白白，这让他表情十分难看。几次想要趁她换气时反驳，却不曾想到，她的肺活量极为惊人，竟一直保持着喋喋不休的状态，没有丝毫停顿。\n" +
                "\n" +
                "    沈贺无奈，只好闷不作声，沉着脸随她骂去。\n" +
                "\n" +
                "    那泼妇足足骂了一刻多钟，直到汉子喊她回家吃饭，这才意犹未尽的啐一口浓痰道：“一天不死出去，就骂侬一天！”说完便摇着肥硕的屁股，吃力的下楼去了。\n" +
                "\n" +
                "    望着她蹒跚离去的背影，沈贺生了半天闷气。突然听到肚子咕咕直叫，便愤愤道：“野蛮粗鲁，简直是不可救药！”这才冲淡了心中的郁闷，朝沈默勉强笑笑道：“潮生，饿坏了吧？”\n" +
                "\n" +
                "    沈默摇摇头，轻声道：“那婆娘为何发飙？我看是故意找茬。”\n" +
                "\n" +
                "    “找茬？确实是。”沈贺苦笑道：“这间阁楼原是她的库房，现在被咱爷俩占了，她当然不高兴了。”\n" +
                "\n" +
                "    “我们住的是她家么？”沈默难以置信道，在他的印象中，老头是个死要面子的书呆子，宁肯搭草棚也不愿寄人篱下那种，怎么突然就转了性呢？\n" +
                "\n" +
                "    “不是，”沈贺神色一黯，不迭摇头道：“这里是沈家大院，我们本家太爷安排咱们住下的……至于那泼妇，跟我们一样，都是投奔本家的，只不过先来欺负后到罢了。”越说表情越黯淡，沈贺不想在儿子面前再说这些，便强打精神道：“莫理她，就当是虎落平阳被犬欺吧。”\n" +
                "\n" +
                "    说着从门后提起个米袋，小心翼翼地倒一些进砂锅里，便默不作声的添水生火，坐在小泥炉边发起了呆，口中似乎还念念有词。\n" +
                "\n" +
                "    沈默能隐约听出，他念的是‘天将降大任于是人也’，便知道老爹心里一定很难受。想说点什么，却不知该如何措辞，只好低声安慰道：“一切都会好起来的。”\n" +
                "\n" +
                "    沈贺身子一僵，使劲点点头，却不再说话。待米粥煮好，他盛大一碗端到沈默面前，轻声问道：“能自己吃吗？”\n" +
                "\n" +
                "    沈默活动下手腕，点点头道：“没问题，手上有些气力了。”\n" +
                "\n" +
                "    沈贺便将碗搁在床沿上，低声道：“慢慢吃，吃完了继续睡。大夫说，睡觉最养人了。”\n" +
                "\n" +
                "    沈默又点点头，见老头端起砂锅，转过身去，背对着自己坐下，似乎在吃饭，似乎在抽泣。";
        tt=tt.replace("\n\n","\n");
        adp.addText(1,tt);
        rp = new ReaderPageAdapter.ReaderPage();
        rp.begin = rp.end = -1;
        rp.chapterIndex = 1;
        adp.addPage(rp);

        ViewPager vp = (ViewPager)findViewById(R.id.reader_viewpager);
        vp.setAdapter(adp);
    }
}
