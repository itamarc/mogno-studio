var myCols='300,*,150';

function resizeFrames(frameSetId) {
	if(myCols == '300,*,150') {
		myCols='300,*,20';
	} else {
		myCols='300,*,150';
	}
	parent.document.getElementById(frameSetId).cols=myCols;
}

