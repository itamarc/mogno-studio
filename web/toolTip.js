function getDim(el){
	for (var lx=0,ly=0; el!=null; lx+=el.offsetLeft,ly+=el.offsetTop,el=el.offsetParent);
	return { x:lx, y:ly }
}

// onmouseover:
function showTip(elem,tip) {
	elem.onmouseout = function() { hideTip() };
	// obter x e y
	var myDim = getDim(elem);
	// obter o div do tooltip
	var ttDiv = document.getElementById("tooltip");
	ttDiv.style.left = myDim.x+10;
	ttDiv.style.top = myDim.y-10;
	// registrar conteúdo
	setContent(ttDiv,tip);
	// mostrar div
	ttDiv.style.display="block";
}

// onmouseout:
function hideTip() {
	// obter o div do tooltip
	var ttDiv = document.getElementById("tooltip");
	// esconder div
	clearElem(ttDiv);
	ttDiv.style.display="none";
}

function clearElem(el) {
	while (el.hasChildNodes()) {
		el.removeChild(el.lastChild);
	}
}

function setContent(el,cont) {
	clearElem(el);
	var textNode = document.createTextNode(cont);
	el.appendChild(textNode);
}

