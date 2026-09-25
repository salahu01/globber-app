import {Composition, Still} from 'remotion';
import {Promo} from './Promo';
import {Reel, Thumbnail} from './Reel';
export const Root = () => (
  <>
  <Composition id="Reel" component={Reel} durationInFrames={1200} fps={30} width={1080} height={1350} />
  <Composition id="Promo" component={Promo} durationInFrames={900} fps={30} width={1080} height={1080} />
  <Still id="Thumbnail" component={Thumbnail} width={1080} height={1350} />
  </>
);
